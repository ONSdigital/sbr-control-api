package uk.gov.ons.sbr.models

import play.api.libs.json.{ JsPath, Reads }
import play.api.libs.functional.syntax._

/**
 * Created by coolit on 22/09/2017.
 */
case class EditEnterprise(
  updatedBy: String,
  updateVars: Map[String, String]
)

object EditEnterprise {
  implicit val editEnterpriseReads: Reads[EditEnterprise] = (
    (JsPath \ "updatedBy").read[String] and
    (JsPath \ "vars").read[Map[String, String]]
  )(EditEnterprise.apply _)
}