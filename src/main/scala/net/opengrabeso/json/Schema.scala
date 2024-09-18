package net.opengrabeso.json

import scala.quoted.*
import scala.reflect.ClassTag

object Schema extends ASchema with TypeAccessScala3 {

  def enumerateSubclassesImpl[T: Type](using Quotes): Expr[List[ClassTag[?]]] = {
    import quotes.reflect.*
    '{ List.empty[ClassTag[?]] }
  }

  inline def enumerateSubclasses[T]: List[ClassTag[?]] = ${ enumerateSubclassesImpl[T] }
  /** helper call for schema derivation of polymorphic sealed trait and classes */
  inline def listDerivedClasses[T]: List[TypeDesc] = enumerateSubclasses[T].map(TypeDesc.apply(_))
}


