package controllers.v1

import controllers.v1.fixture.HttpServerErrorStatusCode
import org.scalatest.{ FreeSpec, Matchers }
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.HttpVerbs.GET
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._

class ReportingUnitRoutingSpec extends FreeSpec with Matchers with GuiceOneAppPerSuite {

  /*
   * When valid arguments are routed to the retrieve... actions, an attempt will be made to connect to HBase.
   * We do not prime HBase in this spec, as we are only concerned with routing.  We therefore override the timeout
   * configuration to minimise the time this spec waits on connection attempts that we know will fail.
   */
  override def fakeApplication() = new GuiceApplicationBuilder().configure(Map("db.hbase-rest.timeout" -> "100")).build()

  private trait Fixture extends HttpServerErrorStatusCode {
    val ValidErn = "1000000012"
    val ValidPeriod = "201802"
    val ValidRurn = "33000000000"
  }

  "A request to retrieve a Reporting Unit by Enterprise reference (ERN), period, and Reporting Unit reference (RURN)" - {
    /*
     * Request should be routed to the "badRequest" action.
     */
    "is rejected when" - {
      "the ERN" - {
        "has fewer than ten digits" ignore new Fixture {
          val ErnTooFewDigits = ValidErn.drop(1)

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnTooFewDigits/periods/$ValidPeriod/reportingunits/$ValidRurn"))

          status(result) shouldBe BAD_REQUEST
        }

        "has more than ten digits" ignore new Fixture {
          val ErnTooManyDigits = ValidErn + "9"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnTooManyDigits/periods/$ValidPeriod/reportingunits/$ValidRurn"))

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" ignore new Fixture {
          val ErnNonNumeric = new String(Array.fill(10)('A'))

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnNonNumeric/periods/$ValidPeriod/reportingunits/$ValidRurn"))

          status(result) shouldBe BAD_REQUEST
        }
      }

      "the RURN" - {
        "has fewer than eleven digits" ignore new Fixture {
          val RurnTooFewDigits = ValidRurn.drop(1)

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$ValidPeriod/reportingunits/$RurnTooFewDigits"))

          status(result) shouldBe BAD_REQUEST
        }

        "has more than eleven digits" ignore new Fixture {
          val RurnTooManyDigits = ValidRurn + "9"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$ValidPeriod/reportingunits/$RurnTooManyDigits"))

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" ignore new Fixture {
          val RurnNonNumeric = new String(Array.fill(11)('Z'))

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$ValidPeriod/reportingunits/$RurnNonNumeric"))

          status(result) shouldBe BAD_REQUEST
        }
      }

      "the Period" - {
        "has fewer than six digits" in new Fixture {
          val PeriodTooFewDigits = ValidPeriod.drop(1)

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodTooFewDigits/reportingunits/$ValidRurn"))

          status(result) shouldBe BAD_REQUEST
        }

        "has more than six digits" in new Fixture {
          val PeriodTooManyDigits = ValidPeriod + "1"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodTooManyDigits/reportingunits/$ValidRurn"))

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" in new Fixture {
          val PeriodNonNumeric = new String(Array.fill(6)('A'))

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodNonNumeric/reportingunits/$ValidRurn"))

          status(result) shouldBe BAD_REQUEST
        }

        "is negative" in new Fixture {
          val PeriodNegative = "-01801"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodNegative/reportingunits/$ValidRurn"))

          status(result) shouldBe BAD_REQUEST
        }

        "has an invalid month value" - {
          "that is too low" in new Fixture {
            val PeriodInvalidBeforeCalendarMonth = "201800"

            val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodInvalidBeforeCalendarMonth/reportingunits/$ValidRurn"))

            status(result) shouldBe BAD_REQUEST
          }

          "that is too high" in new Fixture {
            val PeriodInvalidAfterCalendarMonth = "201813"

            val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodInvalidAfterCalendarMonth/reportingunits/$ValidRurn"))

            status(result) shouldBe BAD_REQUEST
          }
        }
      }
    }

    /*
     * A valid request should be routed to the "retrieveReportingUnit" action.
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
          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$validPeriod/reportingunits/$ValidRurn"))

          status(result) should be(aServerError)
        }
      }
    }
  }

  "A request to find all reporting units for a given Enterprise reference (ERN) and period" - {
    /*
     * Request should be routed to the "badRequest" action.
     */
    "is rejected when" - {
      "the ERN" - {
        "has fewer than ten digits" ignore new Fixture {
          val ErnTooFewDigits = ValidErn.drop(1)

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnTooFewDigits/periods/$ValidPeriod/reportingunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "has more than ten digits" ignore new Fixture {
          val ErnTooManyDigits = ValidErn + "9"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnTooManyDigits/periods/$ValidPeriod/reportingunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" ignore new Fixture {
          val ErnNonNumeric = new String(Array.fill(10)('A'))

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ErnNonNumeric/periods/$ValidPeriod/reportingunits"))

          status(result) shouldBe BAD_REQUEST
        }
      }

      "the Period" - {
        "has fewer than six digits" in new Fixture {
          val PeriodTooFewDigits = ValidPeriod.drop(1)

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodTooFewDigits/reportingunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "has more than six digits" in new Fixture {
          val PeriodTooManyDigits = ValidPeriod + "1"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodTooManyDigits/reportingunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" in new Fixture {
          val PeriodNonNumeric = new String(Array.fill(6)('A'))

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodNonNumeric/reportingunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "is negative" in new Fixture {
          val PeriodNegative = "-01801"

          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodNegative/reportingunits"))

          status(result) shouldBe BAD_REQUEST
        }

        "has an invalid month value" - {
          "that is too low" in new Fixture {
            val PeriodInvalidBeforeCalendarMonth = "201800"

            val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodInvalidBeforeCalendarMonth/reportingunits"))

            status(result) shouldBe BAD_REQUEST
          }

          "that is too high" in new Fixture {
            val PeriodInvalidAfterCalendarMonth = "201813"

            val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$PeriodInvalidAfterCalendarMonth/reportingunits"))

            status(result) shouldBe BAD_REQUEST
          }
        }
      }
    }

    /*
     * A valid request should be routed to the "retrieveAllReportingUnitsForEnterprise" action.
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
          val Some(result) = route(app, FakeRequest(GET, s"/v1/enterprises/$ValidErn/periods/$validPeriod/reportingunits"))

          status(result) should be(aServerError)
        }
      }
    }
  }
}
