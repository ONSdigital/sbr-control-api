package controllers.v1

import controllers.v1.fixture.HttpServerErrorStatusCode
import org.scalatest.{ FreeSpec, Matchers }
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.HttpVerbs.GET
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
class LegalUnitRoutingSpec extends FreeSpec with Matchers with GuiceOneAppPerSuite {

  /*
   * When valid arguments are routed to the retrieve... actions, an attempt will be made to connect to HBase.
   * We do not prime HBase in this spec, as we are only concerned with routing.  We therefore override the timeout
   * configuration to minimise the time this spec waits on connection attempts that we know will fail.
   */
  override def fakeApplication() = new GuiceApplicationBuilder().configure(Map("db.hbase-rest.timeout" -> "100")).build()

  private trait Fixture extends HttpServerErrorStatusCode {
    val ValidErn = "1000000012"
    val ValidPeriod = "201802"
    val ValidUBRN = "0000000011111111"
  }

  "A request to retrieve a Legal Unit by Enterprise reference (ERN), period, and Legal Unit reference (LURN)" - {
    /*
     * Request should be routed to the "badRequest" action.
     */
    "is rejected when" - {
      "the ERN" - {
        "has fewer than ten digits" ignore new Fixture {
          val ErnTooFewDigits = ValidErn.drop(1)

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnTooFewDigits/periods/$ValidPeriod/legalunits/$ValidUBRN"))

          status(result) shouldBe BAD_REQUEST
        }

        "has more than ten digits" ignore new Fixture {
          val ErnTooManyDigits = ValidErn + "9"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnTooManyDigits/periods/$ValidPeriod/legalunits/$ValidUBRN"))

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" ignore new Fixture {
          val ErnNonNumeric = new String(Array.fill(10)('A'))

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnNonNumeric/periods/$ValidPeriod/legalunits/$ValidUBRN"))

          status(result) shouldBe BAD_REQUEST
        }
      }

      "the UBRN" - {
        "has fewer than sixteen digits" ignore new Fixture {
          val UBRNTooFewDigits = ValidUBRN.drop(1)

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$ValidPeriod/legalunits/$UBRNTooFewDigits"))

          status(result) shouldBe BAD_REQUEST
        }

        "has more than sixteen digits" ignore new Fixture {
          val UBRNTooManyDigits = ValidUBRN + "15"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$ValidPeriod/legalunits/$UBRNTooManyDigits"))

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" ignore new Fixture {
          val UBRNNonNumeric = new String(Array.fill(16)('Z'))

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$ValidPeriod/legalunits/$UBRNNonNumeric"))

          status(result) shouldBe BAD_REQUEST
        }
      }

      "the Period" - {
        "has fewer than six digits" in new Fixture {
          val PeriodTooFewDigits = ValidPeriod.drop(1)

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodTooFewDigits/legalunits/$ValidUBRN"))

          status(result) shouldBe BAD_REQUEST
        }

        "has more than six digits" in new Fixture {
          val PeriodTooManyDigits = ValidPeriod + "1"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodTooManyDigits/legalunits/$ValidUBRN"))

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" in new Fixture {
          val PeriodNonNumeric = new String(Array.fill(6)('A'))

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodNonNumeric/legalunits/$ValidUBRN"))

          status(result) shouldBe BAD_REQUEST
        }

        "is negative" in new Fixture {
          val PeriodNegative = "-01801"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodNegative/legalunits/$ValidUBRN"))

          status(result) shouldBe BAD_REQUEST
        }

        "has an invalid month value" - {
          "that is too low" in new Fixture {
            val PeriodInvalidBeforeCalendarMonth = "201800"

            val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodInvalidBeforeCalendarMonth/legalunits/$ValidUBRN"))

            status(result) shouldBe BAD_REQUEST
          }

          "that is too high" in new Fixture {
            val PeriodInvalidAfterCalendarMonth = "201813"

            val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodInvalidAfterCalendarMonth/legalunits/$ValidUBRN"))

            status(result) shouldBe BAD_REQUEST
          }
        }
      }
    }

    /*
     * A valid request should be routed to the "retrieveLegalUnit" action.
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
          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$validPeriod/legalunits/$ValidUBRN"))

          status(result) should be(aServerError)
        }
      }
    }
  }

  "A request to find all legal units for a given Enterprise reference (ERN) and period" - {
    /*
     * Request should be routed to the "badRequest" action.
     */
    "is rejected when" - {
      "the ERN" - {
        "has fewer than ten digits" ignore new Fixture {
          val ErnTooFewDigits = ValidErn.drop(1)

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnTooFewDigits/periods/$ValidPeriod/legalunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "has more than ten digits" ignore new Fixture {
          val ErnTooManyDigits = ValidErn + "9"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnTooManyDigits/periods/$ValidPeriod/legalunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" ignore new Fixture {
          val ErnNonNumeric = new String(Array.fill(10)('A'))

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnNonNumeric/periods/$ValidPeriod/legalunits"))

          status(result) shouldBe BAD_REQUEST
        }
      }

      "the Period" - {
        "has fewer than six digits" in new Fixture {
          val PeriodTooFewDigits = ValidPeriod.drop(1)

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodTooFewDigits/legalunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "has more than six digits" in new Fixture {
          val PeriodTooManyDigits = ValidPeriod + "1"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodTooManyDigits/legalunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" in new Fixture {
          val PeriodNonNumeric = new String(Array.fill(6)('A'))

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodNonNumeric/legalunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "is negative" in new Fixture {
          val PeriodNegative = "-01801"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodNegative/legalunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "has an invalid month value" - {
          "that is too low" in new Fixture {
            val PeriodInvalidBeforeCalendarMonth = "201800"

            val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodInvalidBeforeCalendarMonth/legalunits"))

            status(result) shouldBe BAD_REQUEST
          }

          "that is too high" in new Fixture {
            val PeriodInvalidAfterCalendarMonth = "201813"

            val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodInvalidAfterCalendarMonth/legalunits"))

            status(result) shouldBe BAD_REQUEST
          }
        }
      }
    }

    /*
     * A valid request should be routed to the "retrieveAllLegalUnitsForEnterprise" action.
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
          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$validPeriod/legalunits"))

          status(result) should be(aServerError)
        }
      }
    }
  }
}
