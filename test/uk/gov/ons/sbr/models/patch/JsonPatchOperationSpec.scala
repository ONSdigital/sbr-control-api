package uk.gov.ons.sbr.models.patch

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json._
import uk.gov.ons.sbr.models.patch.OperationTypes.{ Add, Remove, Replace, Test }

class JsonPatchOperationSpec extends FreeSpec with Matchers {

  private trait Fixture {
    def readAsJsonPatchOperation(jsonStr: String): JsResult[JsonPatchOperation] =
      Json.parse(jsonStr).validate[JsonPatchOperation]
  }

  "A JsonPatchOperation" - {
    "can be read from Json" - {
      "when an add operation" in new Fixture {
        val jsonStr = """{"op": "add", "path": "/a/b/c", "value": ["foo", "bar"]}"""

        readAsJsonPatchOperation(jsonStr) shouldBe JsSuccess(JsonPatchOperation(Add, "/a/b/c"))
      }

      "when a remove operation" in new Fixture {
        val jsonStr = """{"op": "remove", "path": "/a/b/c"}"""

        readAsJsonPatchOperation(jsonStr) shouldBe JsSuccess(JsonPatchOperation(Remove, "/a/b/c"))
      }

      "when a replace operation" in new Fixture {
        val jsonStr = """{"op": "replace", "path": "/a/b/c", "value": 42}"""

        readAsJsonPatchOperation(jsonStr) shouldBe JsSuccess(JsonPatchOperation(Replace, "/a/b/c"))
      }

      "when a test operation" in new Fixture {
        val jsonStr = """{"op": "test", "path": "/a/b/c", "value": "foo"}"""

        readAsJsonPatchOperation(jsonStr) shouldBe JsSuccess(JsonPatchOperation(Test, "/a/b/c"))
      }
    }

    "cannot be read from json" - {
      "when not of the correct type" in new Fixture {
        val jsonStr = """["test", "/a/b/c", "foo"]"""

        readAsJsonPatchOperation(jsonStr) shouldBe a[JsError]
      }

      "when op" - {
        "is missing" in new Fixture {
          val jsonStr = """{"path": "/a/b/c", "value": "foo"}"""

          readAsJsonPatchOperation(jsonStr) shouldBe a[JsError]
        }

        "is not a string value" in new Fixture {
          val jsonStr = """{"op": 42, "path": "/a/b/c", "value": "foo"}"""

          readAsJsonPatchOperation(jsonStr) shouldBe a[JsError]
        }

        "is unrecognised" in new Fixture {
          val jsonStr = """{"op": "unknown", "path": "/a/b/c", "value": "foo"}"""

          readAsJsonPatchOperation(jsonStr) shouldBe a[JsError]
        }
      }

      "when path" - {
        "is missing" in new Fixture {
          val jsonStr = """{"op": "test", "value": "foo"}"""

          readAsJsonPatchOperation(jsonStr) shouldBe a[JsError]
        }

        "is not a string value" in new Fixture {
          val jsonStr = """{"op": "test", "path": 42, "value": "foo"}"""

          readAsJsonPatchOperation(jsonStr) shouldBe a[JsError]
        }
      }
    }
  }
}
