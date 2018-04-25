package controllers.v1

import org.scalatest.matchers.{ BeMatcher, MatchResult }
import org.scalatest.{ FreeSpec, Matchers }
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.HttpVerbs.GET
import play.api.http.Status.{ BAD_REQUEST, INSUFFICIENT_STORAGE, INTERNAL_SERVER_ERROR }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._

/*
 * We are relying on the router to perform argument validation for us (via regex constraints).
 * This spec tests that the router is configured correctly.
 *
 * Because we are relying on the router to validate arguments, and the simple routes file configuration does not
 * compose, each and every route must be tested individually.  In addition, some of the regex are non-trivial,
 * and as the saying goes:
 *
 *     "Some people, when confronted with a problem, think 'I know, I'll use regular expressions.'
 *     Now they have two problems."
 *
 * For example, in order to fully test the period regex, we test that each and every possible month is considered
 * valid.  These are downsides of this "router based validation" approach ...
 */
class LocalUnitRoutingSpec extends FreeSpec with Matchers with GuiceOneAppPerSuite {

  /*
   * When valid arguments are routed to the retrieve... actions, an attempt will be made to connect to HBase.
   * We do not prime HBase in this spec, as we are only concerned with routing.  We therefore override the timeout
   * configuration to minimise the time this spec waits on connection attempts that we know will fail.
   */
  override def fakeApplication() = new GuiceApplicationBuilder().configure(Map("db.hbase-rest.timeout" -> "100")).build()

  private trait Fixture {
    val ValidErn = "1000000012"
    val ValidPeriod = "201802"
    val ValidLurn = "900000011"

    /*
     * A matcher for HTTP status codes in the range: Server Error 5xx
     */
    class HttpServerErrorStatusCodeMatcher extends BeMatcher[Int] {
      override def apply(left: Int): MatchResult =
        MatchResult(
          left >= INTERNAL_SERVER_ERROR && left <= INSUFFICIENT_STORAGE,
          s"$left was not a HTTP server error status code",
          s"$left was a HTTP server error status code"
        )
    }

    val aServerError = new HttpServerErrorStatusCodeMatcher
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

  "A request to find all local units for a given Enterprise reference (ERN) and period" - {
    /*
     * Request should be routed to the "badRequest" action.
     */
    "is rejected when" - {
      "the ERN" - {
        "has fewer than ten digits" in new Fixture {
          val ErnTooFewDigits = ValidErn.drop(1)

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnTooFewDigits/periods/$ValidPeriod/localunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "has more than ten digits" in new Fixture {
          val ErnTooManyDigits = ValidErn + "9"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnTooManyDigits/periods/$ValidPeriod/localunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" in new Fixture {
          val ErnNonNumeric = new String(Array.fill(10)('A'))

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnNonNumeric/periods/$ValidPeriod/localunits"))

          status(result) shouldBe BAD_REQUEST
        }
      }

      "the Period" - {
        "has fewer than six digits" in new Fixture {
          val PeriodTooFewDigits = ValidPeriod.drop(1)

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodTooFewDigits/localunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "has more than six digits" in new Fixture {
          val PeriodTooManyDigits = ValidPeriod + "1"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodTooManyDigits/localunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" in new Fixture {
          val PeriodNonNumeric = new String(Array.fill(6)('A'))

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodNonNumeric/localunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "is negative" in new Fixture {
          val PeriodNegative = "-01801"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodNegative/localunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "has an invalid month value" - {
          "that is too low" in new Fixture {
            val PeriodInvalidBeforeCalendarMonth = "201800"

            val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodInvalidBeforeCalendarMonth/localunits"))

            status(result) shouldBe BAD_REQUEST
          }

          "that is too high" in new Fixture {
            val PeriodInvalidAfterCalendarMonth = "201813"

            val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodInvalidAfterCalendarMonth/localunits"))

            status(result) shouldBe BAD_REQUEST
          }
        }
      }
    }

    /*
     * A valid request should be routed to the "retrieveAllLocalUnitsForEnterprise" action.
     * As we are only interested in routing we have not primed a fake HBase, and so the request will still fail.
     * The key for this test is that we accepted the arguments and attempted to perform a database retrieval -
     * thereby generating a "server error" rather than a "client error".
     */
    "is processed when valid" in new Fixture {
      val Year = "2018"
      val Months = Seq("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
      Months.foreach { month =>
        withClue(s"for month $month") {
          val validPeriod = Year + month
          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$validPeriod/localunits"))

          status(result) should be(aServerError)
        }
      }
    }
  }
}
