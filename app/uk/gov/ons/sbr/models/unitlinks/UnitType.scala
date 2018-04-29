package uk.gov.ons.sbr.models.unitlinks

import play.api.libs.json.{ JsString, JsValue, Writes }

trait UnitType

case object CH extends UnitType
case object VAT extends UnitType
case object PAYE extends UnitType

case object ENT extends UnitType
case object LEU extends UnitType
case object LOU extends UnitType
case object REU extends UnitType

object UnitType {
  def parseString(unitTypeStr: String): Option[UnitType] =
    Vector(CH, VAT, PAYE, LEU, ENT, LOU, REU).find(_.toString.equalsIgnoreCase(unitTypeStr))

  /**
   * @throws java.lang.Exception when unitTypeStr cannot be parsed successfully
   */
  def fromString(unitTypeStr: String): UnitType =
    parseString(unitTypeStr).getOrElse(throw new Exception(s"Could not convert $unitTypeStr to UnitType"))

  implicit val writes: Writes[UnitType] = new Writes[UnitType] {
    override def writes(unitType: UnitType): JsValue =
      JsString(unitType.toString)
  }
}