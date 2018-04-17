package controllers.v1

import java.time.Month.FEBRUARY

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FreeSpec, Matchers, OptionValues}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.mvc.Http.MimeTypes.JSON
import repository.LocalUnitRepository
import support.sample.SampleLocalUnit
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.Lurn

import scala.concurrent.Future

class LocalUnitControllerSpec extends FreeSpec with Matchers with MockFactory with OptionValues {

  private trait Fixture extends SampleLocalUnit {
    val TargetErn = Ern("1234567890")
    val TargetPeriod = Period.fromYearMonth(2018, FEBRUARY)
    val TargetLurn = Lurn("987654321")
    val TargetLocalUnit = aLocalUnit(TargetErn, TargetLurn)

    val repository: LocalUnitRepository = mock[LocalUnitRepository]
    val controller = new LocalUnitController(repository)
  }

  "A request" - {
    "to retrieve a Local Unit by Enterprise reference (ERN), period, and Local Unit reference (LURN)" - {
      "returns a JSON representation of the local unit when it is found" in new Fixture {
        (repository.retrieveLocalUnit _).expects(TargetErn, TargetPeriod, TargetLurn).returning(
          Future.successful(Right(Some(TargetLocalUnit)))
        )

        val action = controller.retrieveLocalUnit(TargetErn.value, Period.asString(TargetPeriod), TargetLurn.value)
        val response = action.apply(FakeRequest())

        status(response) shouldBe OK
        contentType(response).value shouldBe JSON
        contentAsJson(response) shouldBe Json.toJson(TargetLocalUnit)
      }

      "returns NOT_FOUND when the local unit cannot be found" in new Fixture {
        (repository.retrieveLocalUnit _).expects(TargetErn, TargetPeriod, TargetLurn).returning(
          Future.successful(Right(None))
        )

        val action = controller.retrieveLocalUnit(TargetErn.value, Period.asString(TargetPeriod), TargetLurn.value)
        val response = action.apply(FakeRequest())

        status(response) shouldBe NOT_FOUND
      }

      "returns GATEWAY_TIMEOUT when the retrieval time exceeds the configured time out" in new Fixture {
        (repository.retrieveLocalUnit _).expects(TargetErn, TargetPeriod, TargetLurn).returning(
          Future.successful(Left("Timeout."))
        )

        val action = controller.retrieveLocalUnit(TargetErn.value, Period.asString(TargetPeriod), TargetLurn.value)
        val response = action.apply(FakeRequest())

        status(response) shouldBe GATEWAY_TIMEOUT
      }

      "returns INTERNAL_SERVER_ERROR when the retrieval fails" in new Fixture {
        (repository.retrieveLocalUnit _).expects(TargetErn, TargetPeriod, TargetLurn).returning(
          Future.successful(Left("Retrieval failed"))
        )

        val action = controller.retrieveLocalUnit(TargetErn.value, Period.asString(TargetPeriod), TargetLurn.value)
        val response = action.apply(FakeRequest())

        status(response) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "to retrieve all local units for an Enterprise reference (ERN) and period" - {
      "returns a JSON representation that contains all found units when multiple local units are found" in new Fixture {
        val localUnits = Seq(TargetLocalUnit, aLocalUnit(TargetErn, Lurn("123456789")))
        (repository.findLocalUnitsForEnterprise _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Right(localUnits))
        )

        val action = controller.retrieveAllLocalUnitsForEnterprise(TargetErn.value, Period.asString(TargetPeriod))
        val response = action.apply(FakeRequest())

        status(response) shouldBe OK
        contentType(response).value shouldBe JSON
        contentAsJson(response) shouldBe Json.toJson(localUnits)
      }

      "returns a JSON representation that contains the single unit when a sole local unit is found" in new Fixture {
        (repository.findLocalUnitsForEnterprise _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Right(Seq(TargetLocalUnit)))
        )

        val action = controller.retrieveAllLocalUnitsForEnterprise(TargetErn.value, Period.asString(TargetPeriod))
        val response = action.apply(FakeRequest())

        status(response) shouldBe OK
        contentType(response).value shouldBe JSON
        contentAsJson(response) shouldBe Json.toJson(Seq(TargetLocalUnit))
      }

      /*
       * See LocalUnitApi for an explanation as to why we expect this behaviour.
       */
      "returns NOT_FOUND when no local units are found" in new Fixture {
        (repository.findLocalUnitsForEnterprise _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Right(Seq.empty))
        )

        val action = controller.retrieveAllLocalUnitsForEnterprise(TargetErn.value, Period.asString(TargetPeriod))
        val response = action.apply(FakeRequest())

        status(response) shouldBe NOT_FOUND
      }

      "returns GATEWAY_TIMEOUT when the search time exceeds the configured time out" in new Fixture {
        (repository.findLocalUnitsForEnterprise _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Left("Timeout."))
        )

        val action = controller.retrieveAllLocalUnitsForEnterprise(TargetErn.value, Period.asString(TargetPeriod))
        val response = action.apply(FakeRequest())

        status(response) shouldBe GATEWAY_TIMEOUT
      }

      "returns INTERNAL_SERVER_ERROR when the search fails" in new Fixture {
        (repository.findLocalUnitsForEnterprise _).expects(TargetErn, TargetPeriod).returning(
          Future.successful(Left("Search failed"))
        )

        val action = controller.retrieveAllLocalUnitsForEnterprise(TargetErn.value, Period.asString(TargetPeriod))
        val response = action.apply(FakeRequest())

        status(response) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    /*
     * This just tests the action.
     * See LocalUnitRoutingSpec for tests that requests are routed correctly between the available actions.
     */
    "containing an invalid argument" - {
      "receives a BAD REQUEST response" in new Fixture {
        val action = controller.badRequest(TargetErn.value, Period.asString(TargetPeriod), Some(TargetLurn.value))
        val response = action.apply(FakeRequest())

        status(response) shouldBe BAD_REQUEST
      }
    }
  }
}