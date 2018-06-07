package uk.gov.ons.sbr.models.legalunit

import play.api.libs.json.{ JsString, JsValue, Writes }

case class UBRN(value: String)

object UBRN {
  implicit val writes = new Writes[UBRN] {
    override def writes(ubrn: UBRN): JsValue =
      JsString(ubrn.value)
  }
  def reverse(ubrn: UBRN): String =
    ubrn.value.reverse
}
