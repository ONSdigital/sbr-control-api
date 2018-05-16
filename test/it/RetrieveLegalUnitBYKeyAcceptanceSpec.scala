import java.time.Month.MARCH

import fixture.ServerAcceptanceSpec
import it.fixture.ReadsLegalUnit.legalUnitReads
import org.scalatest.OptionValues
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status.{ BAD_REQUEST, NOT_FOUND, OK, INTERNAL_SERVER_ERROR }
import play.mvc.Http.MimeTypes.JSON
import repository.hbase.legalunit.LegalUnitColumns._
import repository.hbase.legalunit.LegalUnitQuery
import support.WithWireMockHBase
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.legalunit.{ LegalUnit, UBRN }

class RetrieveLegalUnitBYKeyAcceptanceSpec extends ServerAcceptanceSpec with WithWireMockHBase with OptionValues {

  private val TargetErn = Ern("1000000123")
  private val TargetPeriod = Period.fromYearMonth(2018, MARCH)
  private val TargetUBRN = UBRN("0000000000111111")

  private val LegalUnitSingleMatchHBaseResponseBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${LegalUnitQuery.byRowKey(TargetErn, TargetPeriod, TargetUBRN)}", columns =
          aColumnWith(name = uBRN, value = TargetUBRN.value),
          aColumnWith(name = UBRNref, value = "some-UBRN"),
          aColumnWith(name = ern, value = TargetErn.value),
          aColumnWith(name = entref, value = "some-entref"))
      ).mkString("[", ",", "]")
    }}"""

  info("As an SBR user")
  info("I want to retrieve a legal unit for an enterprise and a period in time")
  info("So that I can view the legal unit details via the user interface")

  feature("retrieve an existing Legal Unit") {
    scenario("by exact Enterprise reference (ERN), period, and Legal Unit reference (UBRN)") { wsClient =>
      Given(s"a legal unit exists with $TargetErn, $TargetPeriod, and $TargetUBRN")
      stubHBaseFor(aLegalUnitRequest(withErn = TargetErn, withPeriod = TargetPeriod, withUBRN = TargetUBRN).willReturn(
        anOkResponse().withBody(LegalUnitSingleMatchHBaseResponseBody)
      ))

      When(s"the legal unit with $TargetErn, $TargetPeriod, and $TargetUBRN is requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/legalunits/${TargetUBRN.value}").get())

      Then(s"the details of the unique legal unit identified by $TargetErn, $TargetPeriod, and $TargetUBRN are returned")
      response.status shouldBe OK
      response.header(CONTENT_TYPE).value shouldBe JSON
      response.json.as[LegalUnit] shouldBe
        LegalUnit(TargetUBRN, UBRNref = Some("some-UBRN"), enterprise = EnterpriseLink(TargetErn, entref = Some("some-entref")))
    }
  }
  feature("retrieve a non-existent Legal Unit") {
    scenario(s"by exact Enterprise reference (ERN), period, and Legal Unit reference (UBRN)") { wsClient =>
      Given(s"a legal unit does not exist with $TargetErn, $TargetPeriod, and $TargetUBRN")
      stubHBaseFor(aLegalUnitRequest(withErn = TargetErn, withPeriod = TargetPeriod, withUBRN = TargetUBRN).willReturn(
        anOkResponse().withBody(NoMatchFoundResponse)
      ))

      When(s"a legal unit with $TargetErn, $TargetPeriod, and $TargetUBRN is requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/legalunits/${TargetUBRN.value}").get())

      Then(s"a NOT FOUND response is returned")
      response.status shouldBe NOT_FOUND
    }
  }
  feature("validate request parameters") {
    scenario(s"rejecting an Enterprise reference (ERN) that is not ten digits long") { wsClient =>
      Given(s"that an ERN is represented by a ten digit number")

      When(s"the user requests a Leqal Unit having an ERN that is not ten digits long")
      val response = await(wsClient.url(s"/v1/enterprises/123456789/periods/${Period.asString(TargetPeriod)}/legalunits/${TargetUBRN.value}").get())

      Then(s"a BAD REQUEST response is returned")
      response.status shouldBe BAD_REQUEST
    }
  }
  feature("handle inability to connect to HBase REST service") {
    scenario("WireMock stops, imitating a loss of connection to HBase") { wsClient =>
      Given("request sent to the HBase REST mock")
      stubHBaseFor(aLegalUnitRequest(withErn = TargetErn, withPeriod = TargetPeriod, withUBRN = TargetUBRN).willReturn(
        anOkResponse().withBody(LegalUnitSingleMatchHBaseResponseBody)
      ))

      When("the mock of HBase stops")
      stopWireMock()
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/legalunits/${TargetUBRN.value}").get())

      Then("an INTERNAL SERVER ERROR response is returned")
      response.status shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
