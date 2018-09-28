package uk.gov.ons.sbr.models.patch

import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Reads }

private[patch] case class JsonPatchOperation(op: OperationType, path: String)

private[patch] object JsonPatchOperation {
  implicit val reads: Reads[JsonPatchOperation] = (
    (JsPath \ "op").read[OperationType] and
    (JsPath \ "path").read[String]
  )(JsonPatchOperation.apply _)
}
