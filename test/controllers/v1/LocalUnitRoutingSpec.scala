package controllers.v1

import org.scalatest.{ FreeSpec, Matchers }
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{ BAD_REQUEST, INTERNAL_SERVER_ERROR }
import play.api.test.FakeRequest
import play.api.test.Helpers._

/*
 * We are relying on the router to perform argument validation for us (via regex constraints).
 * This spec tests that the router is configured correctly.
 */
class LocalUnitRoutingSpec extends FreeSpec with Matchers with GuiceOneAppPerSuite {

  private trait Fixture {
    val ValidErn = "1000000012"
    val ValidPeriod = "201802"
    val ValidLurn = "900000011"
  }

  "A request to retrieve a Local Unit by Enterprise reference (ERN), period, and Local Unit reference (LURN)" - {
    /*
     * Request should be routed to the "badRequest" action.
     */
    "is rejected when" - {
      "the ERN" - {
        "has fewer than ten digits" in new Fixture {
          val ErnTooFewDigits = ValidErn.drop(1)

          val Some(result) = route(app, FakeRequest("GET", s"/v1/enterprises/$ErnTooFewDigits/periods/$ValidPeriod/localunits/$ValidLurn"))

          status(result) shouldBe BAD_REQUEST
        }

        "has too many digits" in new Fixture {
          val ErnTooManyDigits = ValidErn + "9"

          val Some(result) = route(app, FakeRequest("GET", s"/v1/enterprises/$ErnTooManyDigits/periods/$ValidPeriod/localunits/$ValidLurn"))

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" in new Fixture {
          val ErnNonNumeric = new String(Array.fill(10)('A'))

          val Some(result) = route(app, FakeRequest("GET", s"/v1/enterprises/$ErnNonNumeric/periods/$ValidPeriod/localunits/$ValidLurn"))

          status(result) shouldBe BAD_REQUEST
        }
      }
    }

    /*
     * Request should be routed to the "retrieveLocalUnit" action.
     * This will actually fail because we have not primed a fake HBase - but the key test is that such a request is
     * routed differently than the invalid scenarios.
     */
    "is processed when valid" in new Fixture {
      val Some(result) = route(app, FakeRequest("GET", s"/v1/enterprises/$ValidErn/periods/$ValidPeriod/localunits/$ValidLurn"))

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
