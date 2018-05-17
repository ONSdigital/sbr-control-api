package uk.gov.ons.sbr.models.unitlinks

import play.api.libs.json.{ JsString, JsValue, Writes }

case class UnitId(value: String)

object UnitId {
  implicit val writes = new Writes[UnitId] {
    override def writes(unitId: UnitId): JsValue =
      JsString(unitId.value)
  }
}