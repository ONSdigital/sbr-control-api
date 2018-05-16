package uk.gov.ons.sbr.models.reportingunit

import play.api.libs.json.{ JsString, JsValue, Writes }

case class Rurn(value: String)

object Rurn {
  implicit val writes = new Writes[Rurn] {
    override def writes(rurn: Rurn): JsValue =
      JsString(rurn.value)
  }
}

