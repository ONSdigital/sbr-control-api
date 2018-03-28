package uk.gov.ons.sbr.models.enterprise

import play.api.libs.json.{ JsString, JsValue, Writes }

case class Ern(value: String)

object Ern {
  implicit val writes = new Writes[Ern] {
    override def writes(ern: Ern): JsValue =
      JsString(ern.value)
  }

  def reverse(ern: Ern): String =
    ern.value.reverse
}
