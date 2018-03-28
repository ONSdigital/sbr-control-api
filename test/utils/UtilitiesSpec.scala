package utils

import play.api.libs.json.JsObject
import support.TestUtils
import utils.Utilities._

class UtilitiesSpec extends TestUtils {

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

  "unquote" should {
    "removes any unnecessary quotes" in {
      val quoted = """hello this is a\" test"""
      val parsed = unquote(quoted)
      parsed mustNot contain("\"")
    }
  }

}
