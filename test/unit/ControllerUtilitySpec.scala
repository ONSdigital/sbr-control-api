package unit

import java.time.YearMonth
import java.util.Optional

import scala.util.Try

import play.api.{ Application, Configuration }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsNumber
import play.api.mvc.Result
import play.api.test.Helpers._
import com.typesafe.config.ConfigFactory

import utils._
import resource.TestUtils
import services.HBaseDataAccess

/**
 * Created by haqa on 11/08/2017.
 */
class ControllerUtilitySpec extends TestUtils {

  private val validKey = "12446"
  private val validDate = "201711"
  private val searchByIdUrl = "/v1/enterpriseById?id="

  override protected def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .loadConfig(Configuration(ConfigFactory.load))
      .build()

  implicit protected val configuration: Configuration = fakeApplication().configuration

  private val dbTestInstance = new HBaseDataAccess

  "validateYearMonth function" should {
    "return invalid date exception" in {
      val badDate = "209099"
      val validate = dbTestInstance.validateYearMonth(validKey, badDate)
      noException should be thrownBy validate
      validate mustBe a[InvalidReferencePeriod]
    }
    "parse valid string date to YearMonth" in {
      val validate = dbTestInstance.validateYearMonth(validKey, validDate)
      validate must not be a[InvalidReferencePeriod]
      validate mustBe a[ReferencePeriod]
      getParsedRequestType[ReferencePeriod](validate).id mustEqual validKey
      getParsedRequestType[ReferencePeriod](validate).period.toString mustEqual "2017-11"
    }
  }

  "tryAsResponse" should {
    "return a acceptable Result object" in {
      val tryResponse = dbTestInstance.tryAsResponse(Try(toJsonTest("1234")))
      tryResponse mustBe a[Result]
      tryResponse.header.status mustEqual OK
    }
    "execute a failure if the passed function fails in the try" in {
      val failedTry = dbTestInstance.tryAsResponse(Try(toJsonTest("The is not parsable as an Int")))
      failedTry mustBe a[Result]
      noException should be thrownBy failedTry
      failedTry.header.status mustEqual BAD_REQUEST
    }
  }

  "unpackParams" must {
    "return IdRequest instance when key length is right" in {
      implicit val search = requestObject(s"$searchByIdUrl$validKey")
      val unpackedTest = dbTestInstance.matchByParams(Some(validKey), Some(validDate))
      unpackedTest mustBe a[ReferencePeriod]
      getParsedRequestType[ReferencePeriod](unpackedTest).id mustEqual validKey
    }

    "return an InvalidKey instance when no valid key is found" in {
      implicit val search = requestObject(s"${searchByIdUrl}1233")
      val unpackedTest = dbTestInstance.matchByParams(Some("12"))
      unpackedTest must not be a[IdRequest]
      unpackedTest mustBe a[InvalidKey]
    }
  }

  "toOption" should {
    "convert java Optional to Scala Options" ignore {
      val parseDate = dbTestInstance.validateYearMonth(validKey, validDate)
      val convertedDate = getParsedRequestType[ReferencePeriod](parseDate).period
      val ent = EnterpriseTest(validKey, convertedDate)
      val entAsOptional: Optional[EnterpriseTest] = dbTestInstance.toJavaOptional[EnterpriseTest](Some(ent))
      entAsOptional mustBe a[Optional[EnterpriseTest]]
      val entAsOption: Option[EnterpriseTest] = dbTestInstance.toOption[EnterpriseTest](entAsOptional)
      entAsOption must not be a[Optional[EnterpriseTest]]
      entAsOption mustBe a[Option[EnterpriseTest]]
    }
  }

  def getParsedRequestType[T](x: RequestEvaluation): T = x match {
    case (x: T) => x
    case _ => sys.error("Cannot construct to subtype of RequestEvaluation, force failing tests.")
  }

  def toJsonTest(s: String) = JsNumber(s.toInt)

  case class EnterpriseTest(a: String, b: YearMonth)

}
