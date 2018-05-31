import java.time.Month.MARCH

import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import play.mvc.Http.MimeTypes.JSON
import org.scalatest.OptionValues

import uk.gov.ons.sbr.models.enterprise.{EnterpriseLink, Ern}
import uk.gov.ons.sbr.models.legalunit.{LegalUnit, UBRN}
import uk.gov.ons.sbr.models.reportingunit.{ReportingUnitLink, Rurn}
import uk.gov.ons.sbr.models.{Address, Period}

import it.fixture.ServerAcceptanceSpec
import it.fixture.ReadsLegalUnit.legalUnitReads
import repository.hbase.legalunit.LegalUnitColumns._
import repository.hbase.legalunit.LegalUnitQuery
import support.WithWireMockHBase

class RetrieveAllLegalUnitsForEnterpriseAcceptanceSpec extends ServerAcceptanceSpec with WithWireMockHBase with OptionValues {

  private val TargetErn = Ern("1000000123")
  private val TargetPeriod = Period.fromYearMonth(2018, MARCH)
  private val UBRNOne = UBRN("111111110000000")
  private val UBRNTwo = UBRN("000000001111111")

  private val LegalUnitMultipleMatchHBaseResponseBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${LegalUnitQuery.byRowKey(TargetErn, TargetPeriod, UBRNOne)}", columns =
          aColumnWith(name = ubrn, value = UBRNOne.value),
          aColumnWith(name = ern, value = TargetErn.value)),
        aColumnWith(name = crn, value = "one-crn"),
        aColumnWith(name = name, value = "one-name"),
        aColumnWith(name = address1, value = "one-address1"),
        aColumnWith(name = address2, value = "one-address2"),
        aColumnWith(name = address3, value = "one-address3"),
        aColumnWith(name = address4, value = "one-address4"),
        aColumnWith(name = postcode, value = "one-postcode"),
        aColumnWith(name = sic07, value = "one-sic07"),
        aColumnWith(name = jobs, value = "42"),
        aColumnWith(name = legalStatus, value = "one-legalStatus"),
        aColumnWith(name = tradingStatus, value = "one-tradingStatus"),
        aRowWith(key = s"${LegalUnitQuery.byRowKey(TargetErn, TargetPeriod, UBRNTwo)}", columns =
          aColumnWith(name = ubrn, value = UBRNTwo.value),
          aColumnWith(name = ern, value = TargetErn.value),
          aColumnWith(name = entref, value = "two-entref"),
          aColumnWith(name = name, value = "two-name"),
          aColumnWith(name = tradingstyle, value = "two-tradingstyle"),
          aColumnWith(name = address1, value = "two-address1"),
          aColumnWith(name = address2, value = "two-address2"),
          aColumnWith(name = address5, value = "two-address5"),
          aColumnWith(name = postcode, value = "two-postcode"),
          aColumnWith(name = sic07, value = "two-sic07"),
          aColumnWith(name = legalStatus, value = "two-legalStatus"),
          aColumnWith(name = turnover, value = "10"),
          aColumnWith(name = tradingStatus, value = "two-tradingStatus"))
      ).mkString("[", ",", "]")
    }}"""

  info("As a SBR user")
  info("I want to retrieve all legal units for an enterprise and a period in time")
  info("So that I can view the legal unit details via the user interface")

  feature("retrieve Legal Units by exact Enterprise reference (ERN) and period") {
    scenario("when the enterprise has multiple legal units") { wsClient =>
      Given(s"two legal units exist with $TargetErn for $TargetPeriod")

      stubHBaseFor(anAllLegalUnitsForEnterpriseRequest(withErn = TargetErn, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(LegalUnitMultipleMatchHBaseResponseBody)
      ))

      When(s"the legal units with $TargetErn for $TargetPeriod are requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/legalunits").get())

      Then(s"the details of the two legal units with $TargetErn for $TargetPeriod are returned")
      response.status shouldBe OK
      response.header(CONTENT_TYPE).value shouldBe JSON
      response.json.as[Seq[LegalUnit]] should contain theSameElementsAs Seq(
        LegalUnit(UBRNOne, crn = Some("one-crn"), name = "one-name", legalStatus = "one-legalStatus",
          tradingStatus = "one-tradingStatus", tradingstyle = None, sic07 = "one-sic07", turnover = None,
          jobs = Some(42), enterprise = EnterpriseLink(TargetErn, entref = None),
          address = Address(line1 = "one-address1", line2 = Some("one-address2"), line3 = Some("one-address3"),
            line4 = Some("one-address4"), line5 = None, postcode = "one-postcode")),
        LegalUnit(UBRNTwo, crn = None, name = "two-name", legalStatus = "two-legalStatus",
          tradingStatus = "two-tradingStatus", tradingstyle = Some("two-tradingstyle"),
          sic07 = "two-sic07", turnover = Some(10), jobs = None, enterprise = EnterpriseLink(TargetErn, entref = Some("two-entref")),
          address = Address(line1 = "two-address1", line2 = Some("two-address2"), line3 = None,
            line4 = None, line5 = Some("two-address5"), postcode = "two-postcode"))
      )
    }

    info("An existing enterprise and period in time combination should have at least one legal unit")

    scenario("when there is no such combination of Enterprise reference (ERN) and period") { wsClient =>
      Given(s"no legal units exist with $TargetErn for $TargetPeriod")
      stubHBaseFor(anAllLegalUnitsForEnterpriseRequest(withErn = TargetErn, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(NoMatchFoundResponse)
      ))

      When(s"the legal units with $TargetErn for $TargetPeriod are requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/${Period.asString(TargetPeriod)}/legalunits").get())

      Then("a NOT FOUND response is returned")
      response.status shouldBe NOT_FOUND
    }
    scenario("when the specified period is invalid") { wsClient =>
      Given(s"that a valid Period has yyyyMM format")
      val invalidPeriod = "2018-02"
      stubHBaseFor(anAllLegalUnitsForEnterpriseRequest(withErn = TargetErn, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(LegalUnitMultipleMatchHBaseResponseBody)
      ))

      When(s"the legal units with $TargetErn for an invalid period of $invalidPeriod are requested")
      val response = await(wsClient.url(s"/v1/enterprises/${TargetErn.value}/periods/$invalidPeriod/legalunits").get())

      Then(s"a BAD REQUEST response is returned")
      response.status shouldBe BAD_REQUEST
    }
  }
}
