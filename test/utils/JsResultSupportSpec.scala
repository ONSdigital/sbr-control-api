package utils

import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.json.{ JsError, JsResult, JsSuccess }

class JsResultSupportSpec extends FreeSpec with Matchers {

  private trait Fixture {
    val Success: JsResult[Int] = JsSuccess(42)
    val Failure: JsResult[Int] = JsError("failed")
  }

  "Two JsResults " - {
    "can be applied to a function of two arguments via map2" - {
      "returning failure when the first is a failure" in new Fixture {
        JsResultSupport.map2(Failure, Success)(_ + _) shouldBe JsError("failed")
      }

      "returning failure when the second is a failure" in new Fixture {
        JsResultSupport.map2(Success, Failure)(_ + _) shouldBe JsError("failed")
      }

      "returning a success containing the result of applying the function when both results are successes" in new Fixture {
        JsResultSupport.map2(JsSuccess(1), JsSuccess(2))(_ + _) shouldBe JsSuccess(3)
      }
    }
  }
}
