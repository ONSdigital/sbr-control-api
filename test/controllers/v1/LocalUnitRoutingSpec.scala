package controllers.v1

import play.api.http.HttpVerbs.GET
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.scalatest.{ FreeSpec, Matchers }
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import controllers.v1.fixture.HttpServerErrorStatusCode

/*
 * We are relying on the router to perform argument validation for us (via regex constraints).
 * This spec tests that the router is configured correctly.
 */
class LocalUnitRoutingSpec extends FreeSpec with Matchers with GuiceOneAppPerSuite {

  /*
   * When valid arguments are routed to the "retrieveLocalUnit" action, an attempt will be made to connect to HBase.
   * We do not prime HBase in this spec, as we are only concerned with routing.  We therefore override the timeout
   * configuration to minimise the time this spec waits on connection attempts that we know will fail.
   */
  override def fakeApplication() = new GuiceApplicationBuilder().configure(Map("db.hbase-rest.timeout" -> "100")).build()

  private trait Fixture extends HttpServerErrorStatusCode {
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

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnTooFewDigits/periods/$ValidPeriod/localunits/$ValidLurn"))

          status(result) shouldBe BAD_REQUEST
        }

        "has more than ten digits" in new Fixture {
          val ErnTooManyDigits = ValidErn + "9"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnTooManyDigits/periods/$ValidPeriod/localunits/$ValidLurn"))

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" in new Fixture {
          val ErnNonNumeric = new String(Array.fill(10)('A'))

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnNonNumeric/periods/$ValidPeriod/localunits/$ValidLurn"))

          status(result) shouldBe BAD_REQUEST
        }
      }

      "the LURN" - {
        "has fewer than nine digits" in new Fixture {
          val LurnTooFewDigits = ValidLurn.drop(1)

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$ValidPeriod/localunits/$LurnTooFewDigits"))

          status(result) shouldBe BAD_REQUEST
        }

        "has more than nine digits" in new Fixture {
          val LurnTooManyDigits = ValidLurn + "9"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$ValidPeriod/localunits/$LurnTooManyDigits"))

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" in new Fixture {
          val LurnNonNumeric = new String(Array.fill(9)('Z'))

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$ValidPeriod/localunits/$LurnNonNumeric"))

          status(result) shouldBe BAD_REQUEST
        }
      }

      "the Period" - {
        "has fewer than six digits" in new Fixture {
          val PeriodTooFewDigits = ValidPeriod.drop(1)

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodTooFewDigits/localunits/$ValidLurn"))

          status(result) shouldBe BAD_REQUEST
        }

        "has more than six digits" in new Fixture {
          val PeriodTooManyDigits = ValidPeriod + "1"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodTooManyDigits/localunits/$ValidLurn"))

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" in new Fixture {
          val PeriodNonNumeric = new String(Array.fill(6)('A'))

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodNonNumeric/localunits/$ValidLurn"))

          status(result) shouldBe BAD_REQUEST
        }

        "is negative" in new Fixture {
          val PeriodNegative = "-01801"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodNegative/localunits/$ValidLurn"))

          status(result) shouldBe BAD_REQUEST
        }

        "has an invalid month value" - {
          "that is too low" in new Fixture {
            val PeriodInvalidBeforeCalendarMonth = "201800"

            val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodInvalidBeforeCalendarMonth/localunits/$ValidLurn"))

            status(result) shouldBe BAD_REQUEST
          }

          "that is too high" in new Fixture {
            val PeriodInvalidAfterCalendarMonth = "201813"

            val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodInvalidAfterCalendarMonth/localunits/$ValidLurn"))

            status(result) shouldBe BAD_REQUEST
          }
        }
      }
    }

    /*
     * A valid request should be routed to the "retrieveLocalUnit" action.
     * As we are only interested in routing we have not primed a fake HBase, and so the request will still fail.
     * The key for this test is that we accepted the arguments and attempted to perform a database retrieval -
     * thereby generating a "server error" rather than a "client error".
     *
     * Because we are relying on the router to validate arguments, and the period regex is non-trivial, we test that
     * each possible month is considered valid here.  This is a downside of this "router based validation" approach ...
     */
    "is processed when valid" in new Fixture {
      val Year = "2018"
      val Months = Seq("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
      Months.foreach { month =>
        withClue(s"for month $month") {
          val validPeriod = Year + month
          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$validPeriod/localunits/$ValidLurn"))

          status(result) should be(aServerError)
        }
      }
    }
  }
}
