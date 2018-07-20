package controllers.v1

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.scalatest.{ FreeSpec, Matchers }
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.UnitType

import controllers.v1.fixture.HttpServerErrorStatusCode
import support.sample.SampleUnitLinks

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
class UnitLinksRoutingSpec extends FreeSpec with GuiceOneAppPerSuite with Matchers {

  /*
   * When valid arguments are routed to the retrieve... actions, an attempt will be made to connect to HBase.
   * We do not prime HBase in this spec, as we are only concerned with routing.  We therefore override the timeout
   * configuration to minimise the time this spec waits on connection attempts that we know will fail.
   */
  override def fakeApplication(): Application = new GuiceApplicationBuilder().configure(Map("db.hbase-rest.timeout" -> "100")).build()

  private trait Fixture extends HttpServerErrorStatusCode with SampleUnitLinks {
    val MinLengthUnitId = "1234" // PAYE references can be from 4 characters in length
    val MaxLengthUnitId = "1234567890123456"
    val ValidUnitId = SampleUnitId.value
    val ValidPeriod = Period.asString(SamplePeriod)
    val ValidUnitType = UnitType.toAcronym(SampleUnitType)

    def fakeRequest(app: Application = app, method: String = GET, url: String) = route(app, FakeRequest(method, url))
  }

  "A request for retrieving Unit Links by period [yyyyMM], unit type and some unit id" - {

    "accepted when" - {
      "the UnitId" - {
        "includes alpha numeric characters" in new Fixture {
          val alphaNumericUnitId = ValidUnitId + "a1b2"
          val Some(result) = fakeRequest(url = s"/v1/periods/$ValidPeriod/types/$ValidUnitType/units/$alphaNumericUnitId")
          status(result) should be(aServerError)
        }

        "includes special characters" in new Fixture {
          val unitIdWithSpecialCharacters = ValidUnitId + "_a!b0"
          val Some(result) = fakeRequest(url = s"/v1/periods/$ValidPeriod/types/$ValidUnitType/units/$unitIdWithSpecialCharacters")
          status(result) should be(aServerError)
        }

        "has the minimum length" in new Fixture {
          val Some(result) = fakeRequest(url = s"/v1/periods/$ValidPeriod/types/$ValidUnitType/units/$MinLengthUnitId")
          status(result) should be(aServerError)
        }

        "has the maximum length" in new Fixture {
          val Some(result) = fakeRequest(url = s"/v1/periods/$ValidPeriod/types/$ValidUnitType/units/$MaxLengthUnitId")
          status(result) should be(aServerError)
        }
      }

      "the UnitType" - {
        "is valid and is all uppercase" in new Fixture {
          val upperCaseUnitTypes = Seq("ENT", "LEU", "LOU", "REU", "CH", "VAT", "PAYE")
          upperCaseUnitTypes.foreach { unitType =>
            withClue(s"for the UnitType [$unitType]") {
              val Some(result) = fakeRequest(url = s"/v1/periods/$ValidPeriod/types/$unitType/units/$ValidUnitId")
              status(result) should be(aServerError)
            }
          }
        }
      }

      "the Period" - {
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
              val Some(result) = fakeRequest(url = s"/v1/periods/$validPeriod/types/$ValidUnitType/units/$ValidUnitId")
              status(result) should be(aServerError)
            }
          }
        }
      }
    }

    "rejected when" - {
      "the UnitId" - {
        "is too short" in new Fixture {
          val unitIdTooShort = MinLengthUnitId.drop(1)
          val Some(result) = fakeRequest(url = s"/v1/periods/$ValidPeriod/types/$ValidUnitType/units/$unitIdTooShort")
          status(result) shouldBe BAD_REQUEST
        }

        "is too long" in new Fixture {
          val unitIdTooLong = MaxLengthUnitId + "9"
          val Some(result) = fakeRequest(url = s"/v1/periods/$ValidPeriod/types/$ValidUnitType/units/$unitIdTooLong")
          status(result) shouldBe BAD_REQUEST
        }
      }

      "the Period" - {
        "has fewer than six digits" in new Fixture {
          val periodTooFewDigits = ValidPeriod.drop(1)
          val Some(result) = fakeRequest(url = s"/v1/periods/$periodTooFewDigits/types/$ValidUnitType/units/$ValidUnitId")
          status(result) shouldBe BAD_REQUEST
        }

        "has more than six digits" in new Fixture {
          val periodTooManyDigits = ValidPeriod + "1"
          val Some(result) = fakeRequest(url = s"/v1/periods/$periodTooManyDigits/types/$ValidUnitType/units/$ValidUnitId")
          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" in new Fixture {
          val periodNonNumeric = new String(Array.fill(6)('A'))
          val Some(result) = fakeRequest(url = s"/v1/periods/$periodNonNumeric/types/$ValidUnitType/units/$ValidUnitId")
          status(result) shouldBe BAD_REQUEST
        }

        "is negative" in new Fixture {
          val periodNegative = "-01801"
          val Some(result) = fakeRequest(url = s"/v1/periods/$periodNegative/types/$ValidUnitType/units/$ValidUnitId")
          status(result) shouldBe BAD_REQUEST
        }

        "has an invalid month value" - {
          "that is too low" in new Fixture {
            val periodInvalidBeforeCalendarMonth = "201800"
            val Some(result) = fakeRequest(url = s"/v1/periods/$periodInvalidBeforeCalendarMonth/types/$ValidUnitType/units/$ValidUnitId")
            status(result) shouldBe BAD_REQUEST
          }

          "that is too high" in new Fixture {
            val periodInvalidAfterCalendarMonth = "201813"
            val Some(result) = fakeRequest(url = s"/v1/periods/$periodInvalidAfterCalendarMonth/types/$ValidUnitType/units/$ValidUnitId")
            status(result) shouldBe BAD_REQUEST
          }
        }
      }

      "the UnitType" - {
        "is not a valid unit type" in new Fixture {
          val invalidUnitType = "UNKNOWN"
          val Some(result) = fakeRequest(url = s"/v1/periods/$ValidPeriod/types/$invalidUnitType/units/$ValidUnitId")
          status(result) shouldBe BAD_REQUEST
        }

        "is non-alpha" in new Fixture {
          val invalidIntergerUnitType = "1234"
          val Some(result) = fakeRequest(url = s"/v1/periods/$ValidPeriod/types/$invalidIntergerUnitType/units/$ValidUnitId")
          status(result) shouldBe BAD_REQUEST
        }

        "is lowercase" in new Fixture {
          val lowerCaseUnitTypes = Seq("ent", "leu", "lou", "reu", "ch", "vat", "paye")
          lowerCaseUnitTypes.foreach { unitType =>
            withClue(s"for the UnitType [$unitType]") {
              val Some(result) = fakeRequest(url = s"/v1/periods/$ValidPeriod/types/$unitType/units/$ValidUnitId")
              status(result) shouldBe BAD_REQUEST
            }
          }
        }

        "is capitalised" in new Fixture {
          val capitalisedUnitTypes = Seq("Ent", "Leu", "Lou", "Reu", "Ch", "Vat", "Paye")
          capitalisedUnitTypes.foreach { unitType =>
            withClue(s"for the UnitType [$unitType]") {
              val Some(result) = fakeRequest(url = s"/v1/periods/$ValidPeriod/types/$unitType/units/$ValidUnitId")
              status(result) shouldBe BAD_REQUEST
            }
          }
        }
      }
    }
  }
}
