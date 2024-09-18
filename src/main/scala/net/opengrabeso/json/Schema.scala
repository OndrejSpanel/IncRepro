package net.opengrabeso.json

import scala.quoted.*
import scala.reflect.ClassTag

/** Scala 3 specific implementation of ASchema */
object Schema extends ASchema with TypeAccessScala3 {

  def getSurface(quotes: Quotes)(classSymbol: quotes.reflect.Symbol): Option[Expr[ClassTag[?]]] = {
    given q: Quotes = quotes
    import quotes.reflect.*
    val subclassType = classSymbol.typeRef
    //println(s"  Subclass ${subclassType.name} ${classSymbol.flags.is(Flags.Trait)} ${classSymbol.flags.is(Flags.Sealed)}")
    Option.when(!classSymbol.flags.is(Flags.Trait) || !classSymbol.flags.is(Flags.Sealed)) { // instead of sealed traits we will list their derived classes
      val expr = subclassType.asType match {
        case '[t] =>
          Expr.summon[ClassTag[t]] match {
            case Some(classTagExpr) => classTagExpr.asExprOf[ClassTag[?]]
            case None =>
              report.error(s"No ClassTag available for type ${Type.show[t]}")
              '{???}
          }
      }
      //println(s"    Surface of ${subclassType.name} $expr")
      expr
    }
  }

  def enumerateSubclassesImpl[T: Type](using Quotes): Expr[List[ClassTag[?]]] = {
    import quotes.reflect.*

    // Get the symbol of the sealed trait T
    val traitSymbol = TypeRepr.of[T].typeSymbol
    if (traitSymbol.flags.is(Flags.Sealed)) { // Ensure T is a sealed trait and get its subclasses
      //println(s"Subclasses of ${traitSymbol.name}: ${traitSymbol.children.map(_.name).mkString(",")}")
      //val process = traitSymbol.children.drop(1).take(1) // good
      //val process = traitSymbol.children.take(3) // bad
      //val process = traitSymbol.children.take(3).drop(1) // bad
      //val process = traitSymbol.children.take(3).drop(2) // bad
      // Recursive function to get all subclasses, including indirect ones
      def getAllSubclasses(symbol: Symbol): List[Symbol] = {
        symbol.children.flatMap { child =>
          child :: getAllSubclasses(child)
        }
      }

      // documentation seems unclear, it seems children already include indirect children, but not always (AB children missing)
      val process = getAllSubclasses(traitSymbol).distinct

      //println(s"  processing ${process.map(_.name).mkString(",")}")
      val childrenExpr = process.flatMap(getSurface(summon[Quotes])) // 2 good, 3 bad
      Expr.ofList(childrenExpr)
    } else {
      report.error(s"${traitSymbol.fullName} is not a sealed trait")
      '{ List.empty[ClassTag[?]] }
    }
  }

  inline def enumerateSubclasses[T]: List[ClassTag[?]] = ${ enumerateSubclassesImpl[T] }
  /** helper call for schema derivation of polymorphic sealed trait and classes */
  inline def listDerivedClasses[T]: List[TypeDesc] = enumerateSubclasses[T].map(TypeDesc.apply(_))
}


