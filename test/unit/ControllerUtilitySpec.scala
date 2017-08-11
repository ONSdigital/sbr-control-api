package unit

import java.time.DateTimeException

import controllers.v1.ControllerUtils
import org.scalatest.{ FlatSpec, Matchers }
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{ JsString, JsValue }
import utils.{ ReferencePeriod, RequestEvaluation }

/**
 * Created by haqa on 11/08/2017.
 */
class ControllerUtilitySpec extends FlatSpec with Matchers with ControllerUtils {
  /**
   * validateYearMonth
   * tryAsResponse
   * unpackParams
   * optionConverter
   * toScalaList
   * resultMatcher
   */
  private val validKey = "12446"
  private val validDate = "201711"

  "validateYearMonth function" should "return invalid date exception" in {
    val evaluationSubtype = "InvalidReferencePeriod"
    val badDate = "209099"
    val validate = validateYearMonth(validKey, badDate)
    instanceName(validate.getClass.getName) shouldBe evaluationSubtype

  }

  "validateYearMonth function" should "parse valid string date to YearMonth" in {
    val evaluationSubtype = "ReferencePeriod"
    val validate = validateYearMonth(validKey, validDate)
    instanceName(validate.getClass.getName) === evaluationSubtype
    validate.toString should contain(validKey)
    validate.toString should contain("2017-11")
  }

  "tryAsResponse" should "return a acceptable Result object" in {
    val tryResponse = tryAsResponse[String](toJsonTest, "A simple json test")
    val resultType = instanceName(tryResponse.getClass.getName)
    tryResponse.header.status shouldBe OK
    resultType shouldBe "Result"
  }



  def toJsonTest(s: String): JsValue = JsString(s)
  def instanceName(s: String, regex: String = "."): String = s.substring(s.lastIndexOf(regex) + 1)

}
