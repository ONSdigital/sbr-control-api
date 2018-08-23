package uk.gov.ons.sbr.models.patch

import org.scalatest.{ FreeSpec, Matchers }
import play.api.data.validation.ValidationError
import play.api.libs.json._
import uk.gov.ons.sbr.models.patch.OperationTypes.{ Replace, Test }

class ReadsOperationSpec extends FreeSpec with Matchers {

  "An operation specification" - {
    "can be parsed (when valid)" - {
      "when it represents a path test operation" in {
        val jsonOperation = Json.parse("""{"op": "test", "path": "/a/b/c", "value": "foo"}""")

        jsonOperation.as[Operation] shouldBe Operation(Test, "/a/b/c", JsString("foo"))
      }

      "when it represents a path replacement operation" in {
        val jsonOperation = Json.parse("""{"op": "replace", "path": "/c/d/e", "value": 42}""")

        jsonOperation.as[Operation] shouldBe Operation(Replace, "/c/d/e", JsNumber(42))
      }
    }

    "is rejected" - {
      "when it has an incorrect type" in {
        val jsonOperation = Json.parse("""["replace", "/c/d/e", "foo"]""")

        jsonOperation.validate[Operation] shouldBe a[JsError]
      }

      "when the 'op' field" - {
        "is missing" in {
          val jsonOperation = Json.parse("""{"path": "/a/b/c", "value": "foo"}""")

          jsonOperation.validate[Operation] shouldBe a[JsError]
        }

        "is of an incorrect type" in {
          val jsonOperation = Json.parse("""{"op": 42, "path": "/a/b/c", "value": "foo"}""")

          jsonOperation.validate[Operation] shouldBe a[JsError]
        }

        "has an unrecognised value" in {
          val jsonOperation = Json.parse("""{"op": "update", "path": "/a/b/c", "value": "foo"}""")

          jsonOperation.validate[Operation] shouldBe JsError(JsPath \ "op" -> ValidationError("Unrecognised operation [update]"))
        }
      }

      "when the 'path' field" - {
        "is missing" in {
          val jsonOperation = Json.parse("""{"op": "test", "value": "foo"}""")

          jsonOperation.validate[Operation] shouldBe a[JsError]
        }

        "is of an incorrect type" in {
          val jsonOperation = Json.parse("""{"op": "test", "path": 42, "value": "foo"}""")

          jsonOperation.validate[Operation] shouldBe a[JsError]
        }
      }

      "when the 'value' field" - {
        "is missing" in {
          val jsonOperation = Json.parse("""{"op": "test", "path": "/a/b/c"}""")

          jsonOperation.validate[Operation] shouldBe a[JsError]
        }
      }
    }
  }
}
