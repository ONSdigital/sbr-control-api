package uk.gov.ons.sbr.models.legalunit

import play.api.libs.json.{ JsString, JsValue, Writes }

case class Uprn(value: String)

object Uprn {
  implicit val writes = new Writes[Uprn] {
    override def writes(uprn: Uprn): JsValue =
      JsString(uprn.value)
  }
}
