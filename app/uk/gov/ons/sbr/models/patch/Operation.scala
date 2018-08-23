package uk.gov.ons.sbr.models.patch

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Operation(op: OperationType, path: String, value: JsValue)

object Operation {
  implicit val reads: Reads[Operation] = (
    (JsPath \ "op").read[OperationType] and
    (JsPath \ "path").read[String] and
    (JsPath \ "value").read[JsValue]
  )(Operation.apply _)
}