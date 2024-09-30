package net.opengrabeso.json

object Schema extends ASchema with TypeAccessScala3 {

  /** helper call for schema derivation of polymorphic sealed trait and classes */
  inline def listDerivedClasses[T]: List[TypeDesc] = SchemaMacros.enumerateSubclasses[T].map(TypeDesc.apply(_))
}


