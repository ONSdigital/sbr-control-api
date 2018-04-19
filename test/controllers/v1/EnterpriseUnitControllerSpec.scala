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
          Future.successful(Some(TargetEnterpriseUnit))
        )

        val request = controller.retrieveEnterpriseUnit(TargetErn.value, Period.asString(TargetPeriod))
        val response = request.apply(FakeRequest())

        status(response) shouldBe OK
        contentType(response) shouldBe Some(JSON)
        contentAsJson(response) shouldBe Json.toJson(TargetEnterpriseUnit)
      }
    }
  }

}
