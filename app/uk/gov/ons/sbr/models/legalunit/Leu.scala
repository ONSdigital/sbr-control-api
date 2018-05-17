package uk.gov.ons.sbr.models.legalunit

import play.api.libs.json.{ JsString, JsValue, Writes }

case class Leu(value: String)

object Leu {
  implicit val writes: Writes[Leu] = new Writes[Leu] {
    override def writes(leu: Leu): JsValue =
      JsString(leu.value)
  }

  def reverse(leu: Leu): String =
    leu.value.reverse
}