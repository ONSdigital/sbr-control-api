package unit

import play.api.libs.json.JsObject
import utils.Utilities._
import resource.TestUtils

class UtilitiesTestSpec extends TestUtils {

  "errAsJson" should {
    "create a custom err json object" in {
      val status = 400
      val code = "bad_request"
      val msg = "could not process request"
      val errMsg = errAsJson(status, code, msg)
      errMsg mustBe a[JsObject]
      (errMsg \ "code").as[String] mustEqual code
    }
  }

  "getElement" should {
    "return a Long for Option[Long]" in {
      val expected = 5784785784L
      val get = getElement(Some(expected))
      get must not be a[Option[Long]]
      get mustEqual expected
    }
  }

  "unquote" should {
    "removes any unnecessary quotes" in {
      val quoted = """hello this is a\" test"""
      val parsed = unquote(quoted)
      parsed mustNot contain("\"")
    }
  }

}
