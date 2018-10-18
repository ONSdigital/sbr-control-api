package uk.gov.ons.sbr.models.patch

import play.api.libs.json.{ JsResult, JsValue, Reads }

sealed trait Operation {
  val path: String
}

case class AddOperation(path: String, value: JsValue) extends Operation
case class RemoveOperation(path: String) extends Operation
case class ReplaceOperation(path: String, value: JsValue) extends Operation
case class TestOperation(path: String, value: JsValue) extends Operation

object Operation {
  implicit val reads = new Reads[Operation] {
    override def reads(json: JsValue): JsResult[Operation] =
      json.validate[JsonPatchOperation].flatMap { patchOperation =>
        val op = patchOperation.op
        op.createOperation(patchOperation.path, json)
      }
  }
}
