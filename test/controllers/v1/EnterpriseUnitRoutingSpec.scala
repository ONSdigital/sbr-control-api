package controllers.v1

import scala.concurrent.Future

import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.scalatest.{ FreeSpec, Matchers }
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import controllers.v1.fixture.HttpServerErrorStatusCode

class EnterpriseUnitRoutingSpec extends FreeSpec with GuiceOneAppPerSuite with Matchers {

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

  }
}
