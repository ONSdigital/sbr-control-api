package uk.gov.ons.sbr.models.legalunit

import play.api.libs.json.{ JsString, JsValue, Writes }

case class Ubrn(value: String)

object Ubrn {
  implicit val writes = new Writes[Ubrn] {
    override def writes(ubrn: Ubrn): JsValue =
      JsString(ubrn.value)
  }
  def reverse(ubrn: Ubrn): String =
    ubrn.value.reverse
}
