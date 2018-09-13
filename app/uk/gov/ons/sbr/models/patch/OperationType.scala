package uk.gov.ons.sbr.models.patch

import play.api.libs.json.Reads.JsStringReads
import play.api.libs.json._
import uk.gov.ons.sbr.models.patch.OperationTypes.{ Replace, Test }

sealed trait OperationType

object OperationTypes {
  case object Test extends OperationType
  case object Replace extends OperationType
}

object OperationType {
  implicit val reads: Reads[OperationType] = new Reads[OperationType] {
    override def reads(jsValue: JsValue): JsResult[OperationType] =
      JsStringReads.reads(jsValue).flatMap { op =>
        byOp.get(op.value).fold[JsResult[OperationType]](JsError(s"Unrecognised operation [${op.value}]"))(JsSuccess(_))
      }
  }

  private val byOp = Map(
    "test" -> Test,
    "replace" -> Replace
  )
}