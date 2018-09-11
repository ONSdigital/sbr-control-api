package uk.gov.ons.sbr.models.patch

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.{ JsError, JsNumber, JsString, Json }
import uk.gov.ons.sbr.models.patch.OperationTypes.{ Replace, Test }

class ReadsPatchSpec extends FreeSpec with Matchers {

  "A patch specification" - {
    "when valid" - {
      "can be successfully parsed when it contains multiple operations" in {
        val jsonPatch = Json.parse(
          s"""|[{"op": "test", "path": "/a/b/c", "value": "foo"},
              | {"op": "replace", "path": "/a/b/c", "value": 42}]""".stripMargin
        )

        jsonPatch.as[Patch] shouldBe Seq(
          Operation(Test, "/a/b/c", JsString("foo")),
          Operation(Replace, "/a/b/c", JsNumber(42))
        )
      }
    }

    "is rejected" - {
      "when it has an incorrect type" in {
        val invalidJsonPatch = Json.parse(s"""{"op": "test", "path": "/a/b/c", "value": "foo"}""")

        invalidJsonPatch.validate[Patch] shouldBe a[JsError]
      }

      "when an operation" - {
        "has an incorrect type" in {
          val invalidJsonPatch = Json.parse(s"""[["test", "/a/b/c", "foo"]]""")

          invalidJsonPatch.validate[Patch] shouldBe a[JsError]
        }

        "is missing a mandatory field" in {
          val invalidJsonPatch = Json.parse(s"""[{"path": "/a/b/c", "value": "foo"}]""")

          invalidJsonPatch.validate[Patch] shouldBe a[JsError]
        }

        "has an incorrect type for a mandatory field" in {
          val invalidJsonPatch = Json.parse(s"""[{"op": "test", "path": ["a", "b", "c"], "value": "foo"}]""")

          invalidJsonPatch.validate[Patch] shouldBe a[JsError]
        }

        "has an invalid operation type" in {
          val invalidJsonPatch = Json.parse(s"""[{"op": "update", "path": "/a/b/c", "value": "foo"}]""")

          invalidJsonPatch.validate[Patch] shouldBe a[JsError]
        }
      }
    }
  }
}
