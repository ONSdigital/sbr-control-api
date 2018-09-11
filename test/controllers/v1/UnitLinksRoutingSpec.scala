package controllers.v1

import controllers.v1.fixture.HttpServerErrorStatusCode
import org.scalatest.{ FreeSpec, Matchers }
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import parsers.JsonPatchBodyParser.JsonPatchMediaType
import play.api.Application
import play.api.http.HttpVerbs.{ GET, PATCH }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{ Headers, Request, Result }
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import support.sample.SampleUnitLinks
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.UnitType

import scala.concurrent.Future

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
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder().configure(Map("db.hbase-rest.timeout" -> "100")).build()

  private trait Fixture extends HttpServerErrorStatusCode with SampleUnitLinks {
    val ValidPeriod = Period.asString(SamplePeriod)

    def routeRequest(app: Application = app, method: String = GET, url: String): Option[Future[Result]] =
      route(app, FakeRequest(method, url))
  }

  private trait QueryFixture extends Fixture {
    val MinLengthUnitId = "1234" // PAYE references can be from 4 characters in length
    val MaxLengthUnitId = "1234567890123456"
    val ValidUnitId = SampleUnitId.value
    val ValidUnitType = UnitType.toAcronym(SampleUnitType)
  }

  private trait EditFixture extends Fixture {
    val ValidVatRef = "123456789012"

    def fakeEditRequest(uri: String): Request[String] =
      FakeRequest(
        method = PATCH,
        uri = uri,
        headers = Headers(CONTENT_TYPE -> JsonPatchMediaType),
        body = s"""|[{"op": "test", "path": "/parents/LEU", "value": "old-ubrn"},
                   | {"op": "replace", "path": "/parents/LEU", "value": "new-ubrn"}]""".stripMargin
      )
  }

  "A request for retrieving Unit Links by period [yyyyMM], unit type and some unit id" - {

    "accepted when" - {
      "the UnitId" - {
        "includes alpha numeric characters" in new QueryFixture {
          val alphaNumericUnitId = ValidUnitId + "a1b2"

          val Some(result) = routeRequest(url = s"/v1/periods/$ValidPeriod/types/$ValidUnitType/units/$alphaNumericUnitId")

          status(result) should be(aServerError)
        }

        "includes special characters" in new QueryFixture {
          val unitIdWithSpecialCharacters = ValidUnitId + "_a!b0"

          val Some(result) = routeRequest(url = s"/v1/periods/$ValidPeriod/types/$ValidUnitType/units/$unitIdWithSpecialCharacters")

          status(result) should be(aServerError)
        }

        "has the minimum length" in new QueryFixture {
          val Some(result) = routeRequest(url = s"/v1/periods/$ValidPeriod/types/$ValidUnitType/units/$MinLengthUnitId")

          status(result) should be(aServerError)
        }

        "has the maximum length" in new QueryFixture {
          val Some(result) = routeRequest(url = s"/v1/periods/$ValidPeriod/types/$ValidUnitType/units/$MaxLengthUnitId")

          status(result) should be(aServerError)
        }
      }

      "the UnitType" - {
        "is valid and is all uppercase" in new QueryFixture {
          val upperCaseUnitTypes = Seq("ENT", "LEU", "LOU", "REU", "CH", "VAT", "PAYE")
          upperCaseUnitTypes.foreach { unitType =>
            withClue(s"for the UnitType [$unitType]") {
              val Some(result) = routeRequest(url = s"/v1/periods/$ValidPeriod/types/$unitType/units/$ValidUnitId")

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
        "is processed when valid" in new QueryFixture {
          val Year = "2018"
          val Months = Seq("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
          Months.foreach { month =>
            withClue(s"for month $month") {
              val validPeriod = Year + month

              val Some(result) = routeRequest(url = s"/v1/periods/$validPeriod/types/$ValidUnitType/units/$ValidUnitId")

              status(result) should be(aServerError)
            }
          }
        }
      }
    }

    "rejected when" - {
      "the UnitId" - {
        "is too short" in new QueryFixture {
          val unitIdTooShort = MinLengthUnitId.drop(1)

          val Some(result) = routeRequest(url = s"/v1/periods/$ValidPeriod/types/$ValidUnitType/units/$unitIdTooShort")

          status(result) shouldBe BAD_REQUEST
        }

        "is too long" in new QueryFixture {
          val unitIdTooLong = MaxLengthUnitId + "9"

          val Some(result) = routeRequest(url = s"/v1/periods/$ValidPeriod/types/$ValidUnitType/units/$unitIdTooLong")

          status(result) shouldBe BAD_REQUEST
        }
      }

      "the Period" - {
        "has fewer than six digits" in new QueryFixture {
          val periodTooFewDigits = ValidPeriod.drop(1)

          val Some(result) = routeRequest(url = s"/v1/periods/$periodTooFewDigits/types/$ValidUnitType/units/$ValidUnitId")

          status(result) shouldBe BAD_REQUEST
        }

        "has more than six digits" in new QueryFixture {
          val periodTooManyDigits = ValidPeriod + "1"

          val Some(result) = routeRequest(url = s"/v1/periods/$periodTooManyDigits/types/$ValidUnitType/units/$ValidUnitId")

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" in new QueryFixture {
          val periodNonNumeric = new String(Array.fill(6)('A'))

          val Some(result) = routeRequest(url = s"/v1/periods/$periodNonNumeric/types/$ValidUnitType/units/$ValidUnitId")

          status(result) shouldBe BAD_REQUEST
        }

        "is negative" in new QueryFixture {
          val periodNegative = "-01801"

          val Some(result) = routeRequest(url = s"/v1/periods/$periodNegative/types/$ValidUnitType/units/$ValidUnitId")

          status(result) shouldBe BAD_REQUEST
        }

        "has an invalid month value" - {
          "that is too low" in new QueryFixture {
            val periodInvalidBeforeCalendarMonth = "201800"

            val Some(result) = routeRequest(url = s"/v1/periods/$periodInvalidBeforeCalendarMonth/types/$ValidUnitType/units/$ValidUnitId")

            status(result) shouldBe BAD_REQUEST
          }

          "that is too high" in new QueryFixture {
            val periodInvalidAfterCalendarMonth = "201813"

            val Some(result) = routeRequest(url = s"/v1/periods/$periodInvalidAfterCalendarMonth/types/$ValidUnitType/units/$ValidUnitId")

            status(result) shouldBe BAD_REQUEST
          }
        }
      }

      "the UnitType" - {
        "is not a valid unit type" in new QueryFixture {
          val invalidUnitType = "UNKNOWN"

          val Some(result) = routeRequest(url = s"/v1/periods/$ValidPeriod/types/$invalidUnitType/units/$ValidUnitId")

          status(result) shouldBe BAD_REQUEST
        }

        "is non-alpha" in new QueryFixture {
          val invalidIntergerUnitType = "1234"

          val Some(result) = routeRequest(url = s"/v1/periods/$ValidPeriod/types/$invalidIntergerUnitType/units/$ValidUnitId")

          status(result) shouldBe BAD_REQUEST
        }

        "is lowercase" in new QueryFixture {
          val lowerCaseUnitTypes = Seq("ent", "leu", "lou", "reu", "ch", "vat", "paye")
          lowerCaseUnitTypes.foreach { unitType =>
            withClue(s"for the UnitType [$unitType]") {
              val Some(result) = routeRequest(url = s"/v1/periods/$ValidPeriod/types/$unitType/units/$ValidUnitId")

              status(result) shouldBe BAD_REQUEST
            }
          }
        }

        "is capitalised" in new QueryFixture {
          val capitalisedUnitTypes = Seq("Ent", "Leu", "Lou", "Reu", "Ch", "Vat", "Paye")
          capitalisedUnitTypes.foreach { unitType =>
            withClue(s"for the UnitType [$unitType]") {
              val Some(result) = routeRequest(url = s"/v1/periods/$ValidPeriod/types/$unitType/units/$ValidUnitId")

              status(result) shouldBe BAD_REQUEST
            }
          }
        }
      }
    }
  }

  "A request to edit the links from a VAT unit is" - {
    "accepted when valid" in new EditFixture {
      val Year = "2018"
      val Months = Seq("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
      Months.foreach { month =>
        withClue(s"for month $month") {
          val validPeriod = Year + month
          val request = fakeEditRequest(s"/v1/periods/$validPeriod/types/VAT/units/$ValidVatRef")

          val Some(result) = route(app, request)

          status(result) should be(aServerError)
        }
      }
    }

    "rejected when" - {
      "the Period" - {
        "has fewer than six digits" in new EditFixture {
          val request = fakeEditRequest(s"/v1/periods/${ValidPeriod.drop(1)}/types/VAT/units/$ValidVatRef")

          val Some(result) = route(app, request)

          status(result) shouldBe BAD_REQUEST
        }

        "has more than six digits" in new EditFixture {
          val request = fakeEditRequest(s"/v1/periods/${ValidPeriod + "1"}/types/VAT/units/$ValidVatRef")

          val Some(result) = route(app, request)

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" in new EditFixture {
          val request = fakeEditRequest(s"/v1/periods/${new String(Array.fill(6)('A'))}/types/VAT/units/$ValidVatRef")

          val Some(result) = route(app, request)

          status(result) shouldBe BAD_REQUEST
        }

        "is negative" in new EditFixture {
          val request = fakeEditRequest(s"/v1/periods/-01801/types/VAT/units/$ValidVatRef")

          val Some(result) = route(app, request)

          status(result) shouldBe BAD_REQUEST
        }

        "has an invalid month value" - {
          "that is too low" in new EditFixture {
            val request = fakeEditRequest(s"/v1/periods/201800/types/VAT/units/$ValidVatRef")

            val Some(result) = route(app, request)

            status(result) shouldBe BAD_REQUEST
          }

          "that is too high" in new EditFixture {
            val request = fakeEditRequest(s"/v1/periods/201813/types/VAT/units/$ValidVatRef")

            val Some(result) = route(app, request)

            status(result) shouldBe BAD_REQUEST
          }
        }
      }

      "the VAT reference" - {
        "has fewer than twelve digits" in new EditFixture {
          val request = fakeEditRequest(s"/v1/periods/$ValidPeriod/types/VAT/units/${ValidVatRef.drop(1)}")

          val Some(result) = route(app, request)

          status(result) shouldBe BAD_REQUEST
        }

        "has more than twelve digits" in new EditFixture {
          val request = fakeEditRequest(s"/v1/periods/$ValidPeriod/types/VAT/units/${ValidVatRef + "9"}")

          val Some(result) = route(app, request)

          status(result) shouldBe BAD_REQUEST
        }

        "is non-numeric" in new EditFixture {
          val request = fakeEditRequest(s"/v1/periods/$ValidPeriod/types/VAT/units/${new String(Array.fill(12)('A'))}")

          val Some(result) = route(app, request)

          status(result) shouldBe BAD_REQUEST
        }
      }
    }
  }
}