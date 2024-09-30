package net.opengrabeso.json

import scala.quoted.*
import scala.reflect.ClassTag

object SchemaMacros {
  def enumerateSubclassesImpl[T: Type](using Quotes): Expr[List[ClassTag[?]]] = {
    import quotes.reflect.*
    '{ List.empty[ClassTag[?]] }
  }

  inline def enumerateSubclasses[T]: List[ClassTag[?]] = ${ enumerateSubclassesImpl[T] }

}
