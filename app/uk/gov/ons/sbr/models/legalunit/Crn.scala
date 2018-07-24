package uk.gov.ons.sbr.models.legalunit

import play.api.libs.json.{ JsString, JsValue, Writes }

case class Crn(value: String)

object Crn {
  implicit val writes = new Writes[Crn] {
    override def writes(crn: Crn): JsValue =
      JsString(crn.value)
  }
}