package uk.gov.ons.sbr.models.units

import play.api.libs.json.{ Json, OFormat }

/**
 * Created by coolit on 23/02/2018.
 *
 * The UI requires the unitType field to be called type, which is reserved in Scala, hence the back ticks.
 *
 */

sealed trait ChildJsonUnit {
  val `type`: String
  val id: String
}

case class Child(`type`: String, id: String) extends ChildJsonUnit
object Child {
  implicit val unitFormat: OFormat[Child] = Json.format[Child]
}

case class LEU(`type`: String, id: String, children: List[Child]) extends ChildJsonUnit
object LEU {
  implicit val unitFormat: OFormat[LEU] = Json.format[LEU]
}
