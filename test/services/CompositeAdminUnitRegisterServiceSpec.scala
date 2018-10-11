package services

import java.time.Month.SEPTEMBER

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FreeSpec, Matchers }
import uk.gov.ons.sbr.models.unitlinks.UnitId
import uk.gov.ons.sbr.models.unitlinks.UnitType.{ PayAsYouEarn, ValueAddedTax }
import uk.gov.ons.sbr.models.{ Period, UnitKey }

import scala.concurrent.Future

class CompositeAdminUnitRegisterServiceSpec extends FreeSpec with Matchers with MockFactory with ScalaFutures {

  private trait Fixture {
    val RegisterPeriod = Period.fromYearMonth(2018, SEPTEMBER)

    val vatUnitRegisterService = mock[UnitRegisterService]
    val payeUnitRegisterService = mock[UnitRegisterService]
    val compositeRegisterService = new CompositeAdminUnitRegisterService(vatUnitRegisterService, payeUnitRegisterService)
  }

  private trait VatFixture extends Fixture {
    val VatUnitKey = UnitKey(UnitId("1234567890123456"), ValueAddedTax, RegisterPeriod)
  }

  private trait PayeFixture extends Fixture {
    val PayeUnitKey = UnitKey(UnitId("1234ABCD"), PayAsYouEarn, RegisterPeriod)
  }

  "A composite admin unit RegisterService" - {
    "queries the VAT register service for VAT units" - {
      "returning found when the unit is registered" in new VatFixture {
        (vatUnitRegisterService.isRegisteredUnit _).expects(VatUnitKey).returning(Future.successful(UnitFound))

        whenReady(compositeRegisterService.isRegisteredUnit(VatUnitKey)) { result =>
          result shouldBe UnitFound
        }
      }

      "returning not found when the unit is not registered" in new VatFixture {
        (vatUnitRegisterService.isRegisteredUnit _).expects(VatUnitKey).returning(Future.successful(UnitNotFound))

        whenReady(compositeRegisterService.isRegisteredUnit(VatUnitKey)) { result =>
          result shouldBe UnitNotFound
        }
      }

      "returning a failure when the lookup fails" in new VatFixture {
        val failure = UnitRegisterFailure("some failure message")
        (vatUnitRegisterService.isRegisteredUnit _).expects(VatUnitKey).returning(Future.successful(failure))

        whenReady(compositeRegisterService.isRegisteredUnit(VatUnitKey)) { result =>
          result shouldBe failure
        }
      }
    }

    "queries the PAYE register service for PAYE units" - {
      "returning found when the unit is registered" in new PayeFixture {
        (payeUnitRegisterService.isRegisteredUnit _).expects(PayeUnitKey).returning(Future.successful(UnitFound))

        whenReady(compositeRegisterService.isRegisteredUnit(PayeUnitKey)) { result =>
          result shouldBe UnitFound
        }
      }

      "returning not found when the unit is not registered" in new PayeFixture {
        (payeUnitRegisterService.isRegisteredUnit _).expects(PayeUnitKey).returning(Future.successful(UnitNotFound))

        whenReady(compositeRegisterService.isRegisteredUnit(PayeUnitKey)) { result =>
          result shouldBe UnitNotFound
        }
      }

      "returning a failure when the lookup fails" in new PayeFixture {
        val failure = UnitRegisterFailure("some failure message")
        (payeUnitRegisterService.isRegisteredUnit _).expects(PayeUnitKey).returning(Future.successful(failure))

        whenReady(compositeRegisterService.isRegisteredUnit(PayeUnitKey)) { result =>
          result shouldBe failure
        }
      }
    }
  }
}
