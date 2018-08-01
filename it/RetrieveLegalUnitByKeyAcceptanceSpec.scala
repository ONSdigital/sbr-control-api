import java.time.Month.MARCH

import controllers.v1.fixture.HttpServerErrorStatusCode
import fixture.ReadsLegalUnit.legalUnitReads
import fixture.ServerAcceptanceSpec
import org.scalatest.OptionValues
import play.api.http.HeaderNames.CONTENT_TYPE
import play.mvc.Http.MimeTypes.JSON
import repository.hbase.legalunit.LegalUnitColumns._
import repository.hbase.legalunit.LegalUnitQuery
import support.WithWireMockHBase
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.{Crn, LegalUnit, Ubrn, Uprn}
import uk.gov.ons.sbr.models.{Address, Period}

class RetrieveLegalUnitByKeyAcceptanceSpec extends ServerAcceptanceSpec with WithWireMockHBase with HttpServerErrorStatusCode with OptionValues {

  private val TargetErn = Ern("1000000123")
  private val TargetPeriod = Period.fromYearMonth(2018, MARCH)
  private val TargetUbrn = Ubrn("0000000000111111")

  private val LegalUnitSingleMatchHBaseResponseBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${LegalUnitQuery.byRowKey(TargetErn, TargetUbrn)}", columns =
          aColumnWith(name = ubrn, value = TargetUbrn.value),
          aColumnWith(name = crn, value = "01245960"),
          aColumnWith(name = uprn, value = "10023450178"),
          aColumnWith(name = name, value = "Big Box Cereal Ltd"),
          aColumnWith(name = tradingStyle, value = "The Cereal Co"),
          aColumnWith(name = address1, value = "(Rochdale)"),
          aColumnWith(name = address2, value = "37 St Albans St"),
          aColumnWith(name = address3, value = "Rochdale"),
          aColumnWith(name = address4, value = "Lancs"),
          aColumnWith(name = postcode, value = "OL16 1UT"),
          aColumnWith(name = sic07, value = "10612"),
          aColumnWith(name = payeJobs, value = "99"),
          aColumnWith(name = turnover, value = "123"),
          aColumnWith(name = legalStatus, value = "1"),
          aColumnWith(name = tradingStatus, value = "A"),
          aColumnWith(name = birthDate, value = "02/02/2017"),
          aColumnWith(name = deathDate, value = "05/06/2018"),
          aColumnWith(name = deathCode, value = "1"))
      ).mkString("[", ",", "]")
    }}"""

  info("As an SBR user")
  info("I want to retrieve a legal unit for an enterprise and a period in time")
  info("So that I can view the legal unit details via the user interface")

  feature("retrieve an existing Legal Unit") {
    scenario("by exact Enterprise reference (ERN), period, and Legal Unit reference (UBRN)") { wsClient =>
      Given(s"a legal unit exists with $TargetErn, $TargetPeriod, and $TargetUbrn")
      stubHBaseFor(aLegalUnitRequest(withErn = TargetErn, withPeriod = TargetPeriod, withUbrn = TargetUbrn).willReturn(
        anOkResponse().withBody(LegalUnitSingleMatchHBaseResponseBody)
      ))
      When(s"the legal unit with $TargetErn, $TargetPeriod, and $TargetUbrn is requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/legalunits/${TargetUbrn.value}").get())

      Then(s"the details of the unique legal unit identified by $TargetErn, $TargetPeriod, and $TargetUbrn are returned")
      response.status shouldBe OK
      response.header(CONTENT_TYPE).value shouldBe JSON
      response.json.as[LegalUnit] shouldBe
        LegalUnit(ubrn = TargetUbrn, name = "Big Box Cereal Ltd", tradingStyle = Some("The Cereal Co"), sic07 = "10612",
          payeJobs = Some(99), turnover = Some(123), legalStatus = "1", tradingStatus = Some("A"),
          birthDate = "02/02/2017", deathDate = Some("05/06/2018"),
          deathCode = Some("1"), address = Address(line1 = "(Rochdale)", line2 = Some("37 St Albans St"),
            line3 = Some("Rochdale"), line4 = Some("Lancs"), line5 = None, postcode = "OL16 1UT"),
          crn = Some(Crn("01245960")), uprn = Some(Uprn("10023450178")))
    }
  }

  feature("retrieve a non-existent Legal Unit") {
    scenario(s"by exact Enterprise reference (ERN), period, and Legal Unit reference (UBRN)") { wsClient =>
      Given(s"a legal unit does not exist with $TargetErn, $TargetPeriod, and $TargetUbrn")
      stubHBaseFor(aLegalUnitRequest(withErn = TargetErn, withPeriod = TargetPeriod, withUbrn = TargetUbrn).willReturn(
        anOkResponse().withBody(NoMatchFoundResponse)
      ))

      When(s"a legal unit with $TargetErn, $TargetPeriod, and $TargetUbrn is requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/legalunits/${TargetUbrn.value}").get())

      Then(s"a NOT FOUND response is returned")
      response.status shouldBe NOT_FOUND
    }
  }

  feature("validate request parameters") {
    scenario(s"rejecting an Enterprise reference (ERN) that is not ten digits long") { wsClient =>
      Given(s"that an ERN is represented by a ten digit number")

      When(s"the user requests a Leqal Unit having an ERN that is not ten digits long")
      val response = await(wsClient.url(s"/v1/enterprises/123456789/periods/${Period.asString(TargetPeriod)}/legalunits/${TargetUbrn.value}").get())

      Then(s"a BAD REQUEST response is returned")
      response.status shouldBe BAD_REQUEST
    }
  }

  feature("handle inability to connect to HBase REST service") {
    scenario("WireMock stops, imitating a loss of connection to HBase") { wsClient =>
      Given("the legal unit repository is unavailable")
      stopWireMock()

      When("the user requests a legal unit")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/legalunits/${TargetUbrn.value}").get())

      Then("a server error is returned")
      response.status shouldBe aServerError
    }
  }
}
