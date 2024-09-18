package net.opengrabeso.p
package state

import net.opengrabeso.json.Schema

object WC {

  /** edit this comment HERE- to trigger compilation */
  sealed trait SimClass {
    def name: String = this.toString
  }

  object SimClass {
    def derivedClasses = Schema.listDerivedClasses[SimClass]

    trait AB
  }
}

