package uk.gov.ons.sbr.models.unitlinks

import play.api.libs.json._

case class UnitId(value: String)

object UnitId {
  implicit val format = new Format[UnitId] {
    override def writes(unitId: UnitId): JsValue =
      JsString(unitId.value)

    override def reads(json: JsValue): JsResult[UnitId] = {
      Reads.JsStringReads.reads(json).map { jsStr =>
        UnitId(jsStr.value)
      }
    }
  }
}