package net.opengrabeso.engine

import net.opengrabeso.p.OM

import scala.language.implicitConversions

object MTTx {
  class DirectSkeleton
}

import MTTx.*

//noinspection ScalaRedundantConversion
trait MTTx extends MT

object MTOpaque {

  opaque type Vector3f = tx.Vector3
}
