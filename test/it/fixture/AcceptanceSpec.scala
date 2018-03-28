package fixture

import org.scalatest.{ GivenWhenThen, Matchers, _ }

abstract class AcceptanceSpec extends fixture.FeatureSpec with GivenWhenThen with Matchers
