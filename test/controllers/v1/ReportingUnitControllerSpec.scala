package controllers.v1

import java.time.Month.FEBRUARY

import org.scalamock.scalatest.MockFactory
import org.scalatest.{ FreeSpec, Matchers, OptionValues }
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.mvc.Http.MimeTypes.JSON
import repository.ReportingUnitRepository
import support.sample.SampleReportingUnit
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.Rurn

import scala.concurrent.Future

class ReportingUnitControllerSpec extends FreeSpec with Matchers with MockFactory with OptionValues {

  private trait Fixture extends SampleReportingUnit {
    val TargetErn = Ern("1234567890")
    val TargetPeriod = Period.fromYearMonth(2018, FEBRUARY)
    val TargetRurn = Rurn("33000000000")
    val TargetReportingUnit = aReportingUnit(TargetErn, TargetRurn)

    val repository: ReportingUnitRepository = mock[ReportingUnitRepository]
    val controller = new ReportingUnitController(repository)
  }

  "A request" - {
    "to retrieve a Reporting Unit by Enterprise reference (ERN), period, and Reporting Unit reference (RURN)" - {
      "returns a JSON representation of the reporting unit when it is found" in new Fixture {
        (repository.retrieveReportingUnit _).expects(TargetErn, TargetPeriod, TargetRurn).returning(
          Future.successful(Right(Some(TargetReportingUnit)))
        )

        val action = controller.retrieveReportingUnit(TargetErn.value, Period.asString(TargetPeriod), TargetRurn.value)
        val response = action.apply(FakeRequest())

        status(response) shouldBe OK
        contentType(response).value shouldBe JSON
        contentAsJson(response) shouldBe Json.toJson(TargetReportingUnit)
      }

      "returns NOT_FOUND when the reporting unit cannot be found" in new Fixture {
        (repository.retrieveReportingUnit _).expects(TargetErn, TargetPeriod, TargetRurn).returning(
          Future.successful(Right(None))
        )

        val action = controller.retrieveReportingUnit(TargetErn.value, Period.asString(TargetPeriod), TargetRurn.value)
        val response = action.apply(FakeRequest())

        status(response) shouldBe NOT_FOUND
      }

      "returns GATEWAY_TIMEOUT when the retrieval time exceeds the configured time out" in new Fixture {
        (repository.retrieveReportingUnit _).expects(TargetErn, TargetPeriod, TargetRurn).returning(
          Future.successful(Left("Timeout."))
        )

        val action = controller.retrieveReportingUnit(TargetErn.value, Period.asString(TargetPeriod), TargetRurn.value)
        val response = action.apply(FakeRequest())

        status(response) shouldBe GATEWAY_TIMEOUT
      }

      "returns INTERNAL_SERVER_ERROR when the retrieval fails" in new Fixture {
        (repository.retrieveReportingUnit _).expects(TargetErn, TargetPeriod, TargetRurn).returning(
          Future.successful(Left("Retrieval failed"))
        )

        val action = controller.retrieveReportingUnit(TargetErn.value, Period.asString(TargetPeriod), TargetRurn.value)
        val response = action.apply(FakeRequest())

        status(response) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "to retrieve all reporting units for an Enterprise reference (ERN) and period" - {
      "returns a JSON representation that contains all found units when multiple reporting units are found" in new Fixture {
        val reportingUnits = Seq(TargetReportingUnit, aReportingUnit(TargetErn, Rurn("33000000000")))
        (repository.findReportingUnitsForEnterprise _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Right(reportingUnits))
        )

        val action = controller.retrieveAllReportingUnitsForEnterprise(TargetErn.value, Period.asString(TargetPeriod))
        val response = action.apply(FakeRequest())

        status(response) shouldBe OK
        contentType(response).value shouldBe JSON
        contentAsJson(response) shouldBe Json.toJson(reportingUnits)
      }

      "returns a JSON representation that contains the single unit when a sole reporting unit is found" in new Fixture {
        (repository.findReportingUnitsForEnterprise _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Right(Seq(TargetReportingUnit)))
        )

        val action = controller.retrieveAllReportingUnitsForEnterprise(TargetErn.value, Period.asString(TargetPeriod))
        val response = action.apply(FakeRequest())

        status(response) shouldBe OK
        contentType(response).value shouldBe JSON
        contentAsJson(response) shouldBe Json.toJson(Seq(TargetReportingUnit))
      }

      "returns NOT_FOUND when no reporting units are found" in new Fixture {
        (repository.findReportingUnitsForEnterprise _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Right(Seq.empty))
        )

        val action = controller.retrieveAllReportingUnitsForEnterprise(TargetErn.value, Period.asString(TargetPeriod))
        val response = action.apply(FakeRequest())

        status(response) shouldBe NOT_FOUND
      }

      "returns GATEWAY_TIMEOUT when the search time exceeds the configured time out" in new Fixture {
        (repository.findReportingUnitsForEnterprise _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Left("Timeout."))
        )

        val action = controller.retrieveAllReportingUnitsForEnterprise(TargetErn.value, Period.asString(TargetPeriod))
        val response = action.apply(FakeRequest())

        status(response) shouldBe GATEWAY_TIMEOUT
      }

      "returns INTERNAL_SERVER_ERROR when the search fails" in new Fixture {
        (repository.findReportingUnitsForEnterprise _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Left("Search failed"))
        )

        val action = controller.retrieveAllReportingUnitsForEnterprise(TargetErn.value, Period.asString(TargetPeriod))
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
        val action = controller.badRequest(TargetErn.value, Period.asString(TargetPeriod), Some(TargetRurn.value))
        val response = action.apply(FakeRequest())

        status(response) shouldBe BAD_REQUEST
      }
    }
  }
}