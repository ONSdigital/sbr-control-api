package uk.gov.ons.sbr.models.patch

import play.api.libs.json.Reads.JsStringReads
import play.api.libs.json._
import uk.gov.ons.sbr.models.patch.OperationTypes.{ Add, Replace, Test }

sealed trait OperationType

object OperationTypes {
  case object Add extends OperationType
  case object Replace extends OperationType
  case object Test extends OperationType
}

object OperationType {
  implicit val reads: Reads[OperationType] = new Reads[OperationType] {
    override def reads(jsValue: JsValue): JsResult[OperationType] =
      JsStringReads.reads(jsValue).flatMap { op =>
        val optOperationType = toOperationType.lift(op.value)
        optOperationType.fold[JsResult[OperationType]](JsError(s"Unrecognised operation [${op.value}]"))(JsSuccess(_))
      }
  }

  private def toOperationType: PartialFunction[String, OperationType] = {
    case "add" => Add
    case "test" => Test
    case "replace" => Replace
  }
}