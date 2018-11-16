package services

import java.time.Month.AUGUST

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.ws.{ WSClient, WSRequest, WSResponse }
import uk.gov.ons.sbr.models.unitlinks.UnitId
import uk.gov.ons.sbr.models.unitlinks.UnitType.ValueAddedTax
import uk.gov.ons.sbr.models.{ Period, UnitKey }
import utils.BaseUrl

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

/*
 * This spec mocks the wsClient, which disregards the rule "don't mock types you don't own" (see "Growing
 * Object-Oriented Software, Guided by Tests" by Freeman & Pryce).  Prefer the sibling test that uses Wiremock
 * where possible.  This was introduced to allow us to assert that the configured host / port are used,
 * as the wsTestClient used by the acceptance test overrides these.
 */
class AdminUnitRegisterService_MockClientSpec extends FreeSpec with Matchers with MockFactory with ScalaFutures {

  private trait Fixture {
    val Protocol = "http"
    val Host = "somehost"
    val Port = 2345
    val AdminServiceBaseUrl = BaseUrl(Protocol, Host, Port, prefix = None)
    val TargetUnitKey = UnitKey(UnitId("123456789012"), ValueAddedTax, Period.fromYearMonth(2018, AUGUST))
    val AwaitTime = 500.milliseconds

    val wsClient = stub[WSClient]
    val wsResponse = stub[WSResponse]
    val wsRequest = mock[WSRequest]
    val adminUnitRegisterService = new AdminUnitRegisterService(AdminServiceBaseUrl, wsClient)
  }

  "An AdminUnit RegisterService" - {
    "targets the specified host and port when making a request" in new Fixture {
      (wsClient.url _).when(where[String](_.startsWith(s"$Protocol://$Host:$Port"))).returning(wsRequest)
      (wsRequest.withHttpHeaders _).expects(*).returning(wsRequest)
      (wsRequest.head _).expects().returning(Future.successful(wsResponse))

      Await.result(adminUnitRegisterService.isRegisteredUnit(TargetUnitKey), AwaitTime)
    }

    /*
     * Any connection failed / socket disconnected type issue will likely result in the WsRequest's
     * Future failing.  This tests the "catch-all" case, and that we can effectively recover the Future.
     */
    "materialises an error into a failure" in new Fixture {
      val errorMessage = "Connection failed"
      (wsClient.url _).when(where[String](_.startsWith(s"$Protocol://$Host:$Port"))).returning(wsRequest)
      (wsRequest.withHttpHeaders _).expects(*).returning(wsRequest)
      (wsRequest.head _).expects().returning(Future.failed(new Exception(errorMessage)))

      whenReady(adminUnitRegisterService.isRegisteredUnit(TargetUnitKey)) { result =>
        result shouldBe UnitRegisterFailure(errorMessage)
      }
    }
  }
}
