package controllers.v1

import scala.concurrent.Future

import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.scalatest.{ FreeSpec, Matchers }
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import controllers.v1.fixture.HttpServerErrorStatusCode

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
class EnterpriseUnitRoutingSpec extends FreeSpec with GuiceOneAppPerSuite with Matchers {

  /*
   * When valid arguments are routed to the retrieve... actions, an attempt will be made to connect to HBase.
   * We do not prime HBase in this spec, as we are only concerned with routing.  We therefore override the timeout
   * configuration to minimise the time this spec waits on connection attempts that we know will fail.
   */
  override def fakeApplication() = new GuiceApplicationBuilder().configure(Map("db.hbase-rest.timeout" -> "100")).build()

  private trait Fixture extends HttpServerErrorStatusCode {
    val ValidErn = "1000000012"
    val ValidPeriod = "201803"

    val fakeRequest: (String) => Option[Future[Result]] = (url: String) => route(app, FakeRequest(GET, url))
  }

  "A request to retrieve a Enterprise Unit by an Enterprise reference number (ERN) and period (yyyyMM)" - {
    "is rejected when" - {
      "the ERN is" - {
        "has fewer than 10 digits" in new Fixture {
          val ErnWithFewerDigits = ValidErn.drop(1)
          val Some(result) = fakeRequest(s"/v1/periods/$ValidPeriod/enterprises/$ErnWithFewerDigits")

          status(result) shouldBe BAD_REQUEST
        }

        "has more than 10 digits" in new Fixture {
          val ErnWithMoreDigits = ValidErn + "11"
          val Some(result) = fakeRequest(s"/v1/periods/$ValidPeriod/enterprises/$ErnWithMoreDigits")

          status(result) shouldBe BAD_REQUEST
        }

        "a non-numerical value" in new Fixture {
          val ErnWithNonNumericalValues = new String(Array.fill(ValidErn.length)('A'))
          val Some(result) = fakeRequest(s"/v1/periods/$ValidPeriod/enterprises/$ErnWithNonNumericalValues")

          status(result) shouldBe BAD_REQUEST
        }
      }

      "the Period" - {
        "has fewer than six digits" in new Fixture {
          val PeriodTooFewDigits = ValidPeriod.drop(1)

          val Some(result) = fakeRequest(s"/v1/periods/$PeriodTooFewDigits/enterprises/$ValidErn")

          status(result) shouldBe BAD_REQUEST
        }

        "has more than six digits" in new Fixture {
          val PeriodTooManyDigits = ValidPeriod + "1"

          val Some(result) = fakeRequest(s"/v1/periods/$PeriodTooManyDigits/enterprises/$ValidErn")

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" in new Fixture {
          val PeriodNonNumeric = new String(Array.fill(6)('A'))

          val Some(result) = fakeRequest(s"/v1/periods/$PeriodNonNumeric/enterprises/$ValidErn")

          status(result) shouldBe BAD_REQUEST
        }

        "is negative" in new Fixture {
          val PeriodNegative = "-01801"

          val Some(result) = fakeRequest(s"/v1/periods/$PeriodNegative/enterprises/$ValidErn")

          status(result) shouldBe BAD_REQUEST
        }

        "has an invalid month value" - {
          "that is too low" in new Fixture {
            val PeriodInvalidBeforeCalendarMonth = "201800"

            val Some(result) = fakeRequest(s"/v1/periods/$PeriodInvalidBeforeCalendarMonth/enterprises/$ValidErn")

            status(result) shouldBe BAD_REQUEST
          }

          "that is too high" in new Fixture {
            val PeriodInvalidAfterCalendarMonth = "201813"

            val Some(result) = fakeRequest(s"/v1/periods/$PeriodInvalidAfterCalendarMonth/enterprises/$ValidErn")

            status(result) shouldBe BAD_REQUEST
          }
        }
      }
    }

    /*
     * A valid request should be routed to the "retrieveEnterpriseUnit" action.
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
          val Some(result) = fakeRequest(s"/v1/periods/$validPeriod/enterprises/$ValidErn")

          status(result) should be(aServerError)
        }
      }
    }
  }
}
