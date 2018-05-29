package fixture

import org.scalatest.{ GivenWhenThen, Matchers, _ }

trait AcceptanceSpec extends fixture.FeatureSpec with GivenWhenThen with Matchers
