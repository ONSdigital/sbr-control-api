package services

import java.time.Month.AUGUST

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, Outcome }
import play.api.http.Port
import play.api.test.WsTestClient
import support.wiremock.WireMockAdminDataApi
import uk.gov.ons.sbr.models.unitlinks.UnitId
import uk.gov.ons.sbr.models.unitlinks.UnitType.ValueAddedTax
import uk.gov.ons.sbr.models.{ Period, UnitKey }
import utils.BaseUrl

class AdminUnitRegisterService_WiremockSpec extends org.scalatest.fixture.FreeSpec with WireMockAdminDataApi with Matchers with ScalaFutures {

  private val AdminDataServiceBaseUrl = BaseUrl(protocol = "http", host = "localhost", port = DefaultAdminDataApiPort, prefix = None)
  private val AdminUnitRef = UnitId("123456789012")
  private val RegisterPeriod = Period.fromYearMonth(2018, AUGUST)
  private val AdminUnitKey = UnitKey(AdminUnitRef, ValueAddedTax, RegisterPeriod)

  protected case class FixtureParam(adminUnitRegisterService: AdminUnitRegisterService)

  override protected def withFixture(test: OneArgTest): Outcome = {
    withWireMockAdminDataApi { () =>
      WsTestClient.withClient { wsClient =>
        withFixture(test.toNoArgTest(FixtureParam(new AdminUnitRegisterService(AdminDataServiceBaseUrl, wsClient))))
      }(new Port(DefaultAdminDataApiPort))
    }
  }

  "An AdminUnit RegisterService" - {
    "returns UnitFound when the admin unit reference is known for the period" in { fixture =>
      stubAdminDataApiFor(anAdminDataLookupRequest(AdminUnitRef, RegisterPeriod).willReturn(anOkResponse()))

      whenReady(fixture.adminUnitRegisterService.isRegisteredUnit(AdminUnitKey)) { result =>
        result shouldBe UnitFound
      }
    }

    "returns UnitNotFound when the admin unit reference is not known for the period" in { fixture =>
      stubAdminDataApiFor(anAdminDataLookupRequest(AdminUnitRef, RegisterPeriod).willReturn(aNotFoundResponse()))

      whenReady(fixture.adminUnitRegisterService.isRegisteredUnit(AdminUnitKey)) { result =>
        result shouldBe UnitNotFound
      }
    }

    "returns UnitRegistryFailure when the admin unit reference cannot be looked up" in { fixture =>
      stubAdminDataApiFor(anAdminDataLookupRequest(AdminUnitRef, RegisterPeriod).willReturn(aServiceUnavailableResponse()))

      whenReady(fixture.adminUnitRegisterService.isRegisteredUnit(AdminUnitKey)) { result =>
        result shouldBe a[UnitRegisterFailure]
      }
    }
  }
}
