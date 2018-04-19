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
import uk.gov.ons.sbr.models.localunit.{LocalUnit, Lurn}
import scala.concurrent.Future

class LocalUnitControllerSpec extends FreeSpec with Matchers with MockFactory with OptionValues {

  private trait Fixture extends SampleLocalUnit {
    val TargetErn = Ern("")
    val TargetPeriod: Period = Period.fromYearMonth(2018, FEBRUARY)
    val TargetLurn = Lurn("")
    val TargetLocalUnit: LocalUnit = aLocalUnit(TargetErn, TargetLurn)

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
  }
}
