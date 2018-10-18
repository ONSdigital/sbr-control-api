package fixture

import org.scalatest.Outcome
import play.api.http.Port
import play.api.libs.ws.WSClient
import play.api.test.WsTestClient
import support.wiremock.WireMockHBase

abstract class AbstractServerAcceptanceSpec extends ServerAcceptanceSpec with WireMockHBase {
  override type FixtureParam = WSClient

  override protected def withFixture(test: OneArgTest): Outcome =
    withWireMockHBase { () =>
      WsTestClient.withClient { wsClient =>
        withFixture(test.toNoArgTest(wsClient))
      }(new Port(port))
    }
}
