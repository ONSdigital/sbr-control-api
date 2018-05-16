package fixture

import java.time.Month.MARCH

import it.fixture.ReadsLegalUnit.legalUnitReads
import org.scalatest.OptionValues
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status.{ BAD_REQUEST, NOT_FOUND, OK }
import play.mvc.Http.MimeTypes.JSON
import repository.hbase.legalunit.LegalUnitColumns.{ entref, _ }
import repository.hbase.legalunit.LegalUnitQuery
import support.WithWireMockHBase
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.legalunit.{ LegalUnit, UBRN }

class RetrieveAllLegalUnitsForEnterpriseAcceptanceSpec extends ServerAcceptanceSpec with WithWireMockHBase with OptionValues {

  private val TargetErn = Ern("1000000123")
  private val TargetPeriod = Period.fromYearMonth(2018, MARCH)
  private val UBRNOne = UBRN("111111110000000")
  private val UBRNTwo = UBRN("000000001111111")

  private val LegalUnitMultipleMatchHBaseResponseBody =
    s"""{"Row": ${
      List(
        aRowWith(key = s"${LegalUnitQuery.byRowKey(TargetErn, TargetPeriod, UBRNOne)}", columns =
          aColumnWith(name = uBRN, value = UBRNOne.value),
          aColumnWith(name = UBRNref, value = s"one-luref"),
          aColumnWith(name = ern, value = TargetErn.value)),
        aRowWith(key = s"${LegalUnitQuery.byRowKey(TargetErn, TargetPeriod, UBRNTwo)}", columns =
          aColumnWith(name = uBRN, value = UBRNTwo.value),
          aColumnWith(name = ern, value = TargetErn.value),
          aColumnWith(name = entref, value = "two-entref"))
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
        LegalUnit(UBRNOne, UBRNref = Some("one-luref"), enterprise = EnterpriseLink(TargetErn, entref = None)),
        LegalUnit(UBRNTwo, UBRNref = None, enterprise = EnterpriseLink(TargetErn, entref = Some("two-entref")))
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