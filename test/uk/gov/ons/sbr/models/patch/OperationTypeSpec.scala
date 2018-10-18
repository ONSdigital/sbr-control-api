package uk.gov.ons.sbr.models.patch

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json._
import uk.gov.ons.sbr.models.patch.OperationTypes.{ Add, Remove, Replace, Test }

class OperationTypeSpec extends FreeSpec with Matchers {

  private trait OperationFixture {
    val path = "/a/b/c"
  }

  "An OperationType" - {
    "can be read from Json" - {
      "when an add operation" in {
        JsString("add").validate[OperationType] shouldBe JsSuccess(Add)
      }

      "when a remove operation" in {
        JsString("remove").validate[OperationType] shouldBe JsSuccess(Remove)
      }

      "when a replace operation" in {
        JsString("replace").validate[OperationType] shouldBe JsSuccess(Replace)
      }

      "when a test operation" in {
        JsString("test").validate[OperationType] shouldBe JsSuccess(Test)
      }
    }

    "cannot be read from json" - {
      "when not of type string" in {
        JsNumber(123).validate[OperationType] shouldBe a[JsError]
      }

      "when unrecognised" in {
        JsString("unknown").validate[OperationType] shouldBe JsError("Unrecognised operation [unknown]")
      }
    }
  }

  "An add operation" - {
    "can be created" in new OperationFixture {
      val json = Json.parse("""{"value": "foo"}""")

      Add.createOperation(path, json).asOpt shouldBe Some(AddOperation(path, value = JsString("foo")))
    }

    "cannot be created" - {
      "when value is missing" in new OperationFixture {
        val json = Json.parse("""{}""")

        Add.createOperation(path, json) shouldBe a[JsError]
      }
    }
  }

  "A remove operation" - {
    "can be created" in new OperationFixture {
      val json = Json.parse("{}") // will be ignored anyway

      Remove.createOperation(path, json).asOpt shouldBe Some(RemoveOperation(path))
    }
  }

  "A replace operation" - {
    "can be created" in new OperationFixture {
      val json = Json.parse("""{"value": "foo"}""")

      Replace.createOperation(path, json).asOpt shouldBe Some(ReplaceOperation(path, value = JsString("foo")))
    }

    "cannot be created" - {
      "when value is missing" in new OperationFixture {
        val json = Json.parse("""{}""")

        Replace.createOperation(path, json) shouldBe a[JsError]
      }
    }
  }

  "A test operation" - {
    "can be created" in new OperationFixture {
      val json = Json.parse("""{"value": "foo"}""")

      Test.createOperation(path, json).asOpt shouldBe Some(TestOperation(path, value = JsString("foo")))
    }

    "cannot be created" - {
      "when value is missing" in new OperationFixture {
        val json = Json.parse("""{}""")

        Test.createOperation(path, json) shouldBe a[JsError]
      }
    }
  }
}
