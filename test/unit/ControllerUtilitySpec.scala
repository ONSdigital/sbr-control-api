package unit

import java.util.Optional

import controllers.v1.ControllerUtils
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.JsNumber
import play.api.mvc.Result
import resource.TestUtils
import uk.gov.ons.sbr.data.domain.{Enterprise, StatisticalUnit}
import utils._

/**
 * Created by haqa on 11/08/2017.
 */
class ControllerUtilitySpec extends TestUtils with ControllerUtils with GuiceOneAppPerSuite {

  private val validKey = "12446"
  private val validDate = "201711"
  private val searchByIdUrl = "/v1/enterpriseById?id="

  "validateYearMonth function" should {
    "return invalid date exception" in {
      val badDate = "209099"
      val validate = validateYearMonth(validKey, badDate)
      noException should be thrownBy validate
      validate mustBe a[InvalidReferencePeriod]
    }
    "parse valid string date to YearMonth" in {
      val validate = validateYearMonth(validKey, validDate)
      validate must not be a[InvalidReferencePeriod];
      validate mustBe a[ReferencePeriod]
      getParsedRequestType[ReferencePeriod](validate).id mustEqual validKey
      getParsedRequestType[ReferencePeriod](validate).period.toString mustEqual "2017-11"
    }
  }

  "tryAsResponse" should {
    "return a acceptable Result object" in {
      val tryResponse = tryAsResponse[String](toJsonTest, "1234")
      tryResponse mustBe a[Result]
      tryResponse.header.status mustEqual OK
    }
    "execute a failure if the passed function fails in the try" in {
      val failedTry = tryAsResponse[String](toJsonTest, "The is not parsable as an Int")
      failedTry mustBe a[Result]
      noException should be thrownBy failedTry
      failedTry.header.status mustEqual BAD_REQUEST
    }
  }

  "unpackParams" should {
    "return IdRequest instance when key length is right" in {
      val id = "1234"
      val search = requestObject(s"$searchByIdUrl$id")
      val unpackedTest = unpackParams(Some(id), search)
      unpackedTest mustBe a[IdRequest]
      getParsedRequestType[IdRequest](unpackedTest).id mustEqual id
    }

    "return an InvalidKey instance when no valid key is found" in {
      val search = requestObject(s"${searchByIdUrl}1233")
      val unpackedTest = unpackParams(Some("12"), search)
      unpackedTest must not be a[InvalidKey]
      unpackedTest mustBe a[InvalidKey]
    }
  }

  "optionConverter" should {
    "convert java Optional to Scala Options" ignore {
      val parseDate = validateYearMonth(validKey, validDate)
      val convertedDate = getParsedRequestType[ReferencePeriod](parseDate).period
      //    Enterprise(convertedDate, "1244")
      val enterprise = ???
      val conversion = optionConverter(enterprise)
      conversion mustBe a[Option[Enterprise]]
    }
  }

  "toScalaList" should {
    "convert optional list to scala list" ignore {
      val rawUnit: Optional[java.util.List[StatisticalUnit]] = ???
      val unitConverted: Option[List[StatisticalUnit]] = toScalaList(rawUnit)
      unitConverted mustBe a[Option[List[StatisticalUnit]]]
      val listOfStatUnits = unitConverted.getOrElse(List())
      listOfStatUnits mustBe a[scala.collection.immutable.List[StatisticalUnit]]
    }
  }

  def getParsedRequestType[T](x: RequestEvaluation): T = x match { case (x: T) => x }

  def toJsonTest(s: String) = JsNumber(s.toInt)

}
