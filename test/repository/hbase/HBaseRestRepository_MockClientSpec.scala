package repository.hbase

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ EitherValues, FreeSpec, Matchers }
import play.api.libs.ws.{ WSClient, WSRequest, WSResponse }

import scala.concurrent.Future
import scala.concurrent.duration._

/*
 * This spec mocks the wsClient, which disregards the rule "don't mock types you don't own" (see "Growing
 * Object-Oriented Software, Guided by Tests" by Freeman & Pryce).  Prefer the sibling test that uses Wiremock
 * where possible.  This was introduced to simplify asserting that the client-side timeout is configured correctly,
 * as this is not observable via Wiremock.  It also allows us to assert that the configured host / port are used,
 * as the wsTestClient used by the acceptance test overrides these.
 */
class HBaseRestRepository_MockClientSpec extends FreeSpec with Matchers with MockFactory with ScalaFutures with EitherValues {

  private trait Fixture {
    val wsClient = stub[WSClient]
    val wsRequest = mock[WSRequest]
    val config = HBaseRestRepositoryConfig(protocolWithHostname = "http://somehost", port = "4321", "namespace",
      "username", "password", timeout = 3321L)

    val restRepository = new HBaseRestRepository(config, wsClient, stub[HBaseResponseReaderMaker])

    (wsRequest.withHeaders _).expects(*).returning(wsRequest)
    (wsRequest.withAuth _).expects(*, *, *).returning(wsRequest)
  }

  "A HBase REST repository" - {
    "specifies the configured client-side timeout when making a request" in new Fixture {
      (wsClient.url _).when(*).returns(wsRequest)
      (wsRequest.withRequestTimeout _).expects(3321L.milliseconds).returning(wsRequest)
      (wsRequest.get _).expects().returning(Future.successful(stub[WSResponse]))

      restRepository.findRows("table", "rowKey", "columnGroup")
    }

    "targets the specified host and port when making a request" in new Fixture {
      (wsClient.url _).when(where[String](_.startsWith("http://somehost:4321"))).returning(wsRequest)
      (wsRequest.withRequestTimeout _).expects(*).returning(wsRequest)
      (wsRequest.get _).expects().returning(Future.successful(stub[WSResponse]))

      restRepository.findRows("table", "rowKey", "columnGroup")
    }

    /*
     * Any connection failed / socket disconnected type issue will likely result in the WsRequest's
     * Future failing.  This tests the "catch-all" case, and that we can effectively recover the Future.
     */
    "materialises a failure into an error message" in new Fixture {
      (wsClient.url _).when(*).returns(wsRequest)
      (wsRequest.withRequestTimeout _).expects(*).returning(wsRequest)
      (wsRequest.get _).expects().returning(Future.failed(new Exception("Connection failed")))

      whenReady(restRepository.findRows("table", "rowKey", "columnGroup")) { result =>
        result.left.value shouldBe "Connection failed"
      }
    }
  }
}
