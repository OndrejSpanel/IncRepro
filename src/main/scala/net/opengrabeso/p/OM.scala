package net.opengrabeso.p

class OM extends OMShaders {

  override def clone(): OM = {
    new OM().copy(this)
  }

  def copy(source: OM): OM = {
    this
  }
}
