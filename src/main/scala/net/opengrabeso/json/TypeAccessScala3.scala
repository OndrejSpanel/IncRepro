package net.opengrabeso.json

import scala.reflect.ClassTag

trait TypeAccessScala3 {
  case class TypeDesc(ct: ClassTag[?])
}
