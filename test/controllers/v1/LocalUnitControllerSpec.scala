package controllers.v1

import java.time.Month.FEBRUARY

import org.scalamock.scalatest.MockFactory
import org.scalatest.{ FreeSpec, Matchers, OptionValues }
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
    val TargetErn = Ern("")
    val TargetPeriod = Period.fromYearMonth(2018, FEBRUARY)
    val TargetLurn = Lurn("")
    val TargetLocalUnit = aLocalUnit(TargetErn, TargetLurn)

    val repository = mock[LocalUnitRepository]
    val controller = new LocalUnitController(repository)
  }

  "A request" - {
    "to retrieve a Local Unit by Enterprise reference (ERN), period, and Local Unit reference (LURN)" - {
      "returns with a JSON representation of the local unit when it is found" in new Fixture {
        (repository.retrieveLocalUnit _).expects(TargetErn, TargetPeriod, TargetLurn).returning(
          Future.successful(Some(TargetLocalUnit))
        )

        val action = controller.retrieveLocalUnit(TargetErn.value, Period.asString(TargetPeriod), TargetLurn.value)
        val response = action.apply(FakeRequest())

        status(response) shouldBe OK
        contentType(response).value shouldBe JSON
        contentAsJson(response) shouldBe Json.toJson(TargetLocalUnit)
      }

      "returns NOT FOUND when the local unit cannot be found" in new Fixture {
        (repository.retrieveLocalUnit _).expects(TargetErn, TargetPeriod, TargetLurn).returning(
          Future.successful(None)
        )

        val action = controller.retrieveLocalUnit(TargetErn.value, Period.asString(TargetPeriod), TargetLurn.value)
        val response = action.apply(FakeRequest())

        status(response) shouldBe NOT_FOUND
      }
    }
  }
}
