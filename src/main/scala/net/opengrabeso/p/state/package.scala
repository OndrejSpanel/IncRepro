package net.opengrabeso.p

package object state {

  extension (WI: WI) {
    def isFor(state: State): Boolean = {
      false
    }
  }

  case class WI()

  case class PWD()
}
