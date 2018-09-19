package uk.gov.ons.sbr.models.patch

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.{ JsError, JsString, Json }

class ReadsPatchSpec extends FreeSpec with Matchers {

  "A patch specification" - {
    "can be successfully parsed (when valid)" - {
      "when it contains a single add operation" in {
        val jsonPatch = Json.parse("""[{"op": "add", "path": "/a/b/c", "value": "foo"}]""")

        jsonPatch.as[Patch] shouldBe Seq(AddOperation(path = "/a/b/c", value = JsString("foo")))
      }

      "when it contains a test & replace operation" in {
        val jsonPatch = Json.parse(
          """|[{"op": "test", "path": "/a/b/c", "value": "foo"},
             | {"op": "replace", "path": "/a/b/c", "value": "bar"}]""".stripMargin
        )

        jsonPatch.as[Patch] shouldBe Seq(
          TestOperation(path = "/a/b/c", value = JsString("foo")),
          ReplaceOperation(path = "/a/b/c", value = JsString("bar"))
        )
      }

      "when it contains a test & remove operation" in {
        val jsonPatch = Json.parse(
          """|[{"op": "test", "path": "/a/b/c", "value": "foo"},
             | {"op": "remove", "path": "/a/b/c"}]""".stripMargin
        )

        jsonPatch.as[Patch] shouldBe Seq(
          TestOperation(path = "/a/b/c", value = JsString("foo")),
          RemoveOperation(path = "/a/b/c")
        )
      }
    }

    "is rejected" - {
      "when it has an incorrect type" in {
        val invalidJsonPatch = Json.parse("""{"op": "test", "path": "/a/b/c", "value": "foo"}""")

        invalidJsonPatch.validate[Patch] shouldBe a[JsError]
      }

      "when an operation" - {
        "has an incorrect type" in {
          val invalidJsonPatch = Json.parse("""[["test", "/a/b/c", "foo"]]""")

          invalidJsonPatch.validate[Patch] shouldBe a[JsError]
        }

        "is missing a mandatory field" in {
          val invalidJsonPatch = Json.parse("""[{"path": "/a/b/c", "value": "foo"}]""")

          invalidJsonPatch.validate[Patch] shouldBe a[JsError]
        }

        "has an incorrect type for a mandatory field" in {
          val invalidJsonPatch = Json.parse("""[{"op": "test", "path": ["a", "b", "c"], "value": "foo"}]""")

          invalidJsonPatch.validate[Patch] shouldBe a[JsError]
        }

        "has an invalid operation type" in {
          val invalidJsonPatch = Json.parse("""[{"op": "update", "path": "/a/b/c", "value": "foo"}]""")

          invalidJsonPatch.validate[Patch] shouldBe a[JsError]
        }
      }
    }
  }
}
