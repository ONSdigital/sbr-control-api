package uk.gov.ons.sbr.models.legalunit

import play.api.libs.json.{ JsString, JsValue, Writes }

case class UBRN(value: String)

object UBRN {
  implicit val writes = new Writes[UBRN] {
    override def writes(uBRN: UBRN): JsValue =
      JsString(uBRN.value)
  }
}
