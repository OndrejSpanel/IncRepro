package net.opengrabeso.p

import net.opengrabeso.p.s.SLog

object World {
  case class ItemPlacement(houseId: Option[Int], passable: Boolean)
}

case class World(
  houses: Map[Int, SLog.HDesc]
)
