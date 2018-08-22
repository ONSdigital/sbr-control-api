package controllers.v1

import scala.concurrent.Future

import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.mvc.Http.MimeTypes.JSON
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ FreeSpec, Matchers, OptionValues }

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.UnitType

import repository.UnitLinksRepository
import support.sample.SampleUnitLinks

class UnitLinksControllerSpec extends FreeSpec with Matchers with MockFactory with OptionValues {

  private trait Fixture extends SampleUnitLinks {
    val repository = mock[UnitLinksRepository]
    val controller = new UnitLinksController(repository)

  }

  "A request" - {
    "to retrieve a Unit Links by Unit Id, period and period)" - {
      "returns a JSON representation of the Unit Links when it is found" in new Fixture {
        (repository.retrieveUnitLinks _).expects(SampleUnitId, SampleUnitType, SamplePeriod).returning(
          Future.successful(Right(Some(SampleUnitLinksWithAllFields)))
        )

        val action = controller.retrieveUnitLinksWithPeriod(SampleUnitId.value, Period.asString(SamplePeriod), UnitType.toAcronym(SampleUnitType))
        val response = action.apply(FakeRequest())

        status(response) shouldBe OK
        contentType(response).value shouldBe JSON
        contentAsJson(response) shouldBe Json.toJson(SampleUnitLinksWithAllFields)
      }

      "returns NOT_FOUND when the Unit Links cannot be found" in new Fixture {
        (repository.retrieveUnitLinks _).expects(SampleUnitId, SampleUnitType, SamplePeriod).returning(
          Future.successful(Right(None))
        )

        val action = controller.retrieveUnitLinksWithPeriod(SampleUnitId.value, Period.asString(SamplePeriod), UnitType.toAcronym(SampleUnitType))
        val response = action.apply(FakeRequest())

        status(response) shouldBe NOT_FOUND
      }

      "returns GATEWAY_TIMEOUT when the retrieval time exceeds the configured time out" in new Fixture {
        (repository.retrieveUnitLinks _).expects(SampleUnitId, SampleUnitType, SamplePeriod).returning(
          Future.successful(Left("Timeout."))
        )

        val action = controller.retrieveUnitLinksWithPeriod(SampleUnitId.value, Period.asString(SamplePeriod), UnitType.toAcronym(SampleUnitType))
        val response = action.apply(FakeRequest())

        status(response) shouldBe GATEWAY_TIMEOUT
      }

      "returns INTERNAL_SERVER_ERROR when the retrieval fails" in new Fixture {
        (repository.retrieveUnitLinks _).expects(SampleUnitId, SampleUnitType, SamplePeriod).returning(
          Future.successful(Left("Retrieval failed"))
        )

        val action = controller.retrieveUnitLinksWithPeriod(SampleUnitId.value, Period.asString(SamplePeriod), UnitType.toAcronym(SampleUnitType))
        val response = action.apply(FakeRequest())

        status(response) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    /*
     * This just tests the action.
     * See ReportingUnitRoutingSpec for tests that requests are routed correctly between the available actions.
     */
    "containing an invalid argument" - {
      "receives a BAD REQUEST response" in new Fixture {
        val action = controller.badRequest(SampleUnitId.value, Period.asString(SamplePeriod), UnitType.toAcronym(SampleUnitType))
        val response = action.apply(FakeRequest())

        status(response) shouldBe BAD_REQUEST
      }
    }
  }
}
