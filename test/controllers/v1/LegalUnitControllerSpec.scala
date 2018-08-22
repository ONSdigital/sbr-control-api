package controllers.v1

import java.time.Month.FEBRUARY

import org.scalamock.scalatest.MockFactory
import org.scalatest.{ FreeSpec, Matchers, OptionValues }
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.mvc.Http.MimeTypes.JSON
import repository.LegalUnitRepository
import support.sample.SampleLegalUnit
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.Ubrn

import scala.concurrent.Future

class LegalUnitControllerSpec extends FreeSpec with Matchers with MockFactory with OptionValues {

  private trait Fixture extends SampleLegalUnit {
    val TargetErn = Ern("1234567890")
    val TargetPeriod = Period.fromYearMonth(2018, FEBRUARY)
    val TargetUbrn = Ubrn("0987654321234567")
    val TargetLegalUnit = aLegalUnit(TargetUbrn)

    val repository: LegalUnitRepository = mock[LegalUnitRepository]
    val controller = new LegalUnitController(repository)
  }

  "A request" - {
    "to retrieve a Legal Unit by Enterprise reference (ERN), period, and Legal Unit reference (UBRN)" - {
      "returns a JSON representation of the legal unit when it is found" in new Fixture {
        (repository.retrieveLegalUnit _).expects(TargetErn, TargetPeriod, TargetUbrn).returning(
          Future.successful(Right(Some(TargetLegalUnit)))
        )
        val action = controller.retrieveLegalUnit(TargetErn.value, Period.asString(TargetPeriod), TargetUbrn.value)
        val response = action.apply(FakeRequest())

        status(response) shouldBe OK
        contentType(response).value shouldBe JSON
        contentAsJson(response) shouldBe Json.toJson(TargetLegalUnit)
      }

      "returns NOT_FOUND when the legal unit cannot be found" in new Fixture {
        (repository.retrieveLegalUnit _).expects(TargetErn, TargetPeriod, TargetUbrn).returning(
          Future.successful(Right(None))
        )

        val action = controller.retrieveLegalUnit(TargetErn.value, Period.asString(TargetPeriod), TargetUbrn.value)
        val response = action.apply(FakeRequest())

        status(response) shouldBe NOT_FOUND
      }

      "returns GATEWAY_TIMEOUT when the retrieval time exceeds the configured time out" in new Fixture {
        (repository.retrieveLegalUnit _).expects(TargetErn, TargetPeriod, TargetUbrn).returning(
          Future.successful(Left("Timeout."))
        )

        val action = controller.retrieveLegalUnit(TargetErn.value, Period.asString(TargetPeriod), TargetUbrn.value)
        val response = action.apply(FakeRequest())

        status(response) shouldBe GATEWAY_TIMEOUT
      }

      "returns INTERNAL_SERVER_ERROR when the retrieval fails" in new Fixture {
        (repository.retrieveLegalUnit _).expects(TargetErn, TargetPeriod, TargetUbrn).returning(
          Future.successful(Left("Retrieval failed"))
        )

        val action = controller.retrieveLegalUnit(TargetErn.value, Period.asString(TargetPeriod), TargetUbrn.value)
        val response = action.apply(FakeRequest())

        status(response) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "to retrieve all legal units for an Enterprise reference (ERN) and period" - {
      "returns a JSON representation that contains all found units when multiple legal units are found" in new Fixture {
        val legalUnits = Seq(TargetLegalUnit, aLegalUnit(Ubrn("123456789")))
        (repository.findLegalUnitsForEnterprise _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Right(legalUnits))
        )

        val action = controller.retrieveAllLegalUnitsForEnterprise(TargetErn.value, Period.asString(TargetPeriod))
        val response = action.apply(FakeRequest())

        status(response) shouldBe OK
        contentType(response).value shouldBe JSON
        contentAsJson(response) shouldBe Json.toJson(legalUnits)
      }

      "returns a JSON representation that contains the single unit when a sole legal unit is found" in new Fixture {
        (repository.findLegalUnitsForEnterprise _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Right(Seq(TargetLegalUnit)))
        )

        val action = controller.retrieveAllLegalUnitsForEnterprise(TargetErn.value, Period.asString(TargetPeriod))
        val response = action.apply(FakeRequest())

        status(response) shouldBe OK
        contentType(response).value shouldBe JSON
        contentAsJson(response) shouldBe Json.toJson(Seq(TargetLegalUnit))
      }

      /*
       * See LegalUnitApi for an explanation as to why we expect this behaviour.
       */
      "returns NOT_FOUND when no legal units are found" in new Fixture {
        (repository.findLegalUnitsForEnterprise _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Right(Seq.empty))
        )

        val action = controller.retrieveAllLegalUnitsForEnterprise(TargetErn.value, Period.asString(TargetPeriod))
        val response = action.apply(FakeRequest())

        status(response) shouldBe NOT_FOUND
      }

      "returns GATEWAY_TIMEOUT when the search time exceeds the configured time out" in new Fixture {
        (repository.findLegalUnitsForEnterprise _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Left("Timeout."))
        )

        val action = controller.retrieveAllLegalUnitsForEnterprise(TargetErn.value, Period.asString(TargetPeriod))
        val response = action.apply(FakeRequest())

        status(response) shouldBe GATEWAY_TIMEOUT
      }

      "returns INTERNAL_SERVER_ERROR when the search fails" in new Fixture {
        (repository.findLegalUnitsForEnterprise _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Left("Search failed"))
        )

        val action = controller.retrieveAllLegalUnitsForEnterprise(TargetErn.value, Period.asString(TargetPeriod))
        val response = action.apply(FakeRequest())

        status(response) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    /*
     * This just tests the action.
     * See LegalUnitRoutingSpec for tests that requests are routed correctly between the available actions.
     */
    "containing an invalid argument" - {
      "receives a BAD REQUEST response" in new Fixture {
        val action = controller.badRequest(TargetErn.value, Period.asString(TargetPeriod), Some(TargetUbrn.value))
        val response = action.apply(FakeRequest())

        status(response) shouldBe BAD_REQUEST
      }
    }
  }
}
