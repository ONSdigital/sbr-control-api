package uk.gov.ons.sbr.models.localunit

import play.api.libs.json.{ JsString, JsValue, Writes }

case class Lurn(value: String)

object Lurn {
  implicit val writes = new Writes[Lurn] {
    override def writes(lurn: Lurn): JsValue =
      JsString(lurn.value)
  }
}
