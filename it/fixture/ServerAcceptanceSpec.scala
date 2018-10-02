package fixture

import org.scalatestplus.play.guice.GuiceOneServerPerTest
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

trait ServerAcceptanceSpec extends AcceptanceSpec with GuiceOneServerPerTest with DefaultAwaitTimeout with FutureAwaits
