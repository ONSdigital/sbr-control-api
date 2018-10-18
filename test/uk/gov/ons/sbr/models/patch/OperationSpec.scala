package uk.gov.ons.sbr.models.patch

import org.scalatest.{ FreeSpec, Matchers }
import play.api.data.validation.ValidationError
import play.api.libs.json._

class OperationSpec extends FreeSpec with Matchers {

  "An operation specification" - {
    "can be parsed (when valid)" - {
      "when it represents a path add operation" in {
        val jsonOperation = Json.parse("""{"op": "add", "path": "/a/b/c", "value": ["foo", "bar"]}""")

        jsonOperation.as[Operation] shouldBe AddOperation(path = "/a/b/c", value = JsArray(
          Seq(JsString("foo"), JsString("bar"))
        ))
      }

      "when it represents a path remove operation" in {
        val jsonOperation = Json.parse("""{"op": "remove", "path": "/c/d/e"}""")

        jsonOperation.as[Operation] shouldBe RemoveOperation(path = "/c/d/e")
      }

      "when it represents a path replace operation" in {
        val jsonOperation = Json.parse("""{"op": "replace", "path": "/a/b/c", "value": 42}""")

        jsonOperation.as[Operation] shouldBe ReplaceOperation(path = "/a/b/c", value = JsNumber(42))
      }

      "when it represents a path test operation" in {
        val jsonOperation = Json.parse("""{"op": "test", "path": "/a/b/c", "value": "foo"}""")

        jsonOperation.as[Operation] shouldBe TestOperation(path = "/a/b/c", value = JsString("foo"))
      }
    }

    "cannot be parsed" - {
      "when it has an incorrect type" in {
        val jsonOperation = Json.parse("""["remove", "/c/d/e"]""")

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
    }
  }
}
