package uk.gov.ons.sbr.models.patch

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.{ JsError, JsNumber, JsString }
import uk.gov.ons.sbr.models.patch.OperationTypes.{ Add, Replace, Test }

class ReadsOperationTypeSpec extends FreeSpec with Matchers {
  "An operation type specification" - {
    "is accepted (when recognised)" - {
      "representing the test type" in {
        JsString("test").as[OperationType] shouldBe Test
      }

      "representing the replace type" in {
        JsString("replace").as[OperationType] shouldBe Replace
      }

      "representing the add type" in {
        JsString("add").as[OperationType] shouldBe Add
      }
    }

    "is rejected" - {
      "when unrecognised" in {
        JsString("update").validate[OperationType] shouldBe JsError("Unrecognised operation [update]")
      }

      "when of an incorrect type" in {
        JsNumber(42).validate[OperationType] shouldBe a[JsError]
      }
    }
  }
}
