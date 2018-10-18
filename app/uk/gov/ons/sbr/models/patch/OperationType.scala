package uk.gov.ons.sbr.models.patch

import play.api.libs.json.Reads.JsStringReads
import play.api.libs.json._
import uk.gov.ons.sbr.models.patch.OperationType.ValueOperationMaker
import uk.gov.ons.sbr.models.patch.OperationTypes.{ Add, Remove, Replace, Test }

private[patch] sealed trait OperationType {
  def createOperation(path: String, json: JsValue): JsResult[Operation]
}

private[patch] object OperationTypes {
  case object Add extends OperationType {
    override def createOperation(path: String, json: JsValue): JsResult[Operation] =
      ValueOperationMaker(AddOperation)(path, json)
  }

  case object Remove extends OperationType {
    /*
     * Remove is the simplest operation comprising only the mandatory values: op & path.
     * (We can therefore ignore the json argument)
     */
    override def createOperation(path: String, json: JsValue): JsResult[Operation] =
      JsSuccess(RemoveOperation(path))
  }

  case object Replace extends OperationType {
    override def createOperation(path: String, json: JsValue): JsResult[Operation] =
      ValueOperationMaker(ReplaceOperation)(path, json)
  }

  case object Test extends OperationType {
    override def createOperation(path: String, json: JsValue): JsResult[Operation] =
      ValueOperationMaker(TestOperation)(path, json)
  }
}

private[patch] object OperationType {
  /*
   * Reads an operation that consists of a value field in addition to the mandatory op & path.
   * (Used by: test, add, replace)
   */
  object ValueOperationMaker {
    def apply[A](f: (String, JsValue) => A)(path: String, json: JsValue): JsResult[A] =
      (JsPath \ "value").read[JsValue].reads(json).map(f(path, _))
  }

  implicit val reads: Reads[OperationType] = new Reads[OperationType] {
    override def reads(jsValue: JsValue): JsResult[OperationType] =
      JsStringReads.reads(jsValue).flatMap { op =>
        val optOperationType = toOperationType.lift(op.value)
        optOperationType.fold[JsResult[OperationType]](JsError(s"Unrecognised operation [${op.value}]"))(JsSuccess(_))
      }
  }

  private def toOperationType: PartialFunction[String, OperationType] = {
    case "add" => Add
    case "remove" => Remove
    case "replace" => Replace
    case "test" => Test
  }
}
