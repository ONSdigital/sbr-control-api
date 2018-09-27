package services

import java.time.Month.AUGUST

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, Outcome }
import play.api.http.Port
import play.api.test.WsTestClient
import support.WireMockAdminDataApi
import uk.gov.ons.sbr.models.unitlinks.UnitId
import uk.gov.ons.sbr.models.unitlinks.UnitType.ValueAddedTax
import uk.gov.ons.sbr.models.{ Period, UnitKey }
import utils.BaseUrl

class VatRegisterService_WiremockSpec extends org.scalatest.fixture.FreeSpec with WireMockAdminDataApi with Matchers with ScalaFutures {

  override val wireMockPort: Int = 9005
  private val VatServiceBaseUrl = BaseUrl(protocol = "http", host = "localhost", port = wireMockPort, prefix = None)
  private val VatRef = UnitId("123456789012")
  private val RegisterPeriod = Period.fromYearMonth(2018, AUGUST)
  private val VatUnitKey = UnitKey(VatRef, ValueAddedTax, RegisterPeriod)

  protected case class FixtureParam(vatRegisterService: VatRegisterService)

  override protected def withFixture(test: OneArgTest): Outcome = {
    WsTestClient.withClient { wsClient =>
      withFixture(test.toNoArgTest(FixtureParam(new VatRegisterService(VatServiceBaseUrl, wsClient))))
    }(new Port(wireMockPort))
  }

  "A VAT RegisterService" - {
    "returns UnitFound when the VAT reference is known for the period" in { fixture =>
      stubAdminDataApiFor(aVatRefLookupRequest(VatRef, RegisterPeriod).willReturn(anOkResponse()))

      whenReady(fixture.vatRegisterService.isRegisteredUnit(VatUnitKey)) { result =>
        result shouldBe UnitFound
      }
    }

    "returns UnitNotFound when the VAT reference is not known for the period" in { fixture =>
      stubAdminDataApiFor(aVatRefLookupRequest(VatRef, RegisterPeriod).willReturn(aNotFoundResponse()))

      whenReady(fixture.vatRegisterService.isRegisteredUnit(VatUnitKey)) { result =>
        result shouldBe UnitNotFound
      }
    }

    "returns UnitRegistryFailure when the VAT reference cannot be looked up" in { fixture =>
      stubAdminDataApiFor(aVatRefLookupRequest(VatRef, RegisterPeriod).willReturn(aServiceUnavailableResponse()))

      whenReady(fixture.vatRegisterService.isRegisteredUnit(VatUnitKey)) { result =>
        result shouldBe a[UnitRegisterFailure]
      }
    }
  }
}
