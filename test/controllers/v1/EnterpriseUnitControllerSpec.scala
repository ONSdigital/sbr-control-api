package controllers.v1

import java.time.Month.FEBRUARY

import scala.concurrent.Future

import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{ contentType, status, _ }
import play.mvc.Http.MimeTypes.JSON
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ FreeSpec, Matchers, OptionValues }

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern }

import repository.EnterpriseUnitRepository
import support.sample.SampleEnterpriseUnit

class EnterpriseUnitControllerSpec extends FreeSpec with Matchers with MockFactory with OptionValues {

  trait Fixture extends SampleEnterpriseUnit {
    val repository: EnterpriseUnitRepository = mock[EnterpriseUnitRepository]
    val TargetErn = Ern("")
    val TargetPeriod: Period = Period.fromYearMonth(2018, FEBRUARY)
    val TargetEnterpriseUnit: Enterprise = aEnterpriseSample(TargetErn)

    val controller = new EnterpriseUnitController(repository)
  }

  "A request" - {
    "to retrieve an Enterprise unit by enterprise reference number (ERN) and period (Period, uuuuMM)" - {
      "returns a JSON representation of an enterprise when found" in new Fixture {
        (repository.retrieveEnterpriseUnit _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Right(Some(TargetEnterpriseUnit)))
        )

        val request = controller.retrieveEnterpriseUnit(TargetErn.value, Period.asString(TargetPeriod))
        val response = request.apply(FakeRequest())

        status(response) shouldBe OK
        contentType(response) shouldBe Some(JSON)
        contentAsJson(response) shouldBe Json.toJson(TargetEnterpriseUnit)
      }

      "returns NOT_FOUND when a valid enterprise unit (ERN) and period is not found" in new Fixture {
        (repository.retrieveEnterpriseUnit _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Right(None))
        )

        val request = controller.retrieveEnterpriseUnit(TargetErn.value, Period.asString(TargetPeriod))
        val response = request.apply(FakeRequest())

        status(response) shouldBe NOT_FOUND
      }

      "returns GatewayTimeout when the retrieval time exceeds the configured time out" in new Fixture {
        (repository.retrieveEnterpriseUnit _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Left("Timeout."))
        )
        val request = controller.retrieveEnterpriseUnit(TargetErn.value, Period.asString(TargetPeriod))
        val response = request.apply(FakeRequest())

        status(response) shouldBe GATEWAY_TIMEOUT
      }

      "returns InternalServerError when an unexpected error occurs during transaction" in new Fixture {
        (repository.retrieveEnterpriseUnit _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Left("Retrieval failed"))
        )

        val request = controller.retrieveEnterpriseUnit(TargetErn.value, Period.asString(TargetPeriod))
        val response = request.apply(FakeRequest())

        status(response) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    /*
     * This just tests the action.
     * See EnterpriseUnitRoutingSpec for tests that requests are routed correctly between the available actions.
     *
     * No mocking - no underlying repository needed stops at controller
     */
    "containing invalid arguments" - {
      "receives a BAD_REQUEST status response" in new Fixture {
        val request = controller.badRequest(TargetErn.value, Period.asString(TargetPeriod))
        val response = request.apply(FakeRequest())

        status(response) shouldBe BAD_REQUEST
      }
    }

  }

}
