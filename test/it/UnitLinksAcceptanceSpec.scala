import java.time.Month.MARCH

import play.api.http.HeaderNames.CONTENT_TYPE
import it.fixture.ReadsUnitLinks._
import play.api.http.Status.OK
import play.mvc.Http.MimeTypes.JSON

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.Leu
import uk.gov.ons.sbr.models.unitlinks.UnitType
import uk.gov.ons.sbr.models.unitlinks.UnitLinks

import fixture.ServerAcceptanceSpec
import repository.hbase.unitlinks.UnitLinksRowKey
import support.WithWireMockHBase

class UnitLinksAcceptanceSpec extends ServerAcceptanceSpec with WithWireMockHBase {

  private val TargetLeu = Leu("1000000012")
  private val TargetPeriod = Period.fromYearMonth(2018, MARCH)
  private val TargetLegalUnitType = UnitType.LegalUnit

  private val ErnParent = Ern("1000000123")
  private val VatId = "401347263289"
  private val CHId = "01752564"
  private val EntParentUnitType = UnitType.Enterprise
  private val CHChildUnitTypeAsString = UnitType.toAcronym(UnitType.CompaniesHouse)
  private val VATChildUnitTypeAsString = UnitType.toAcronym(UnitType.ValueAddedTax)

  private val UnitLinksSingleMatchHBaseResponseBody =
    s"""{ "Row": ${
      List(
        aRowWith(key = s"${UnitLinksRowKey(TargetLeu.value, TargetLegalUnitType, TargetPeriod)}", columns =
          aColumnWith(name = aParentUnitTypeWithPrefix(EntParentUnitType), value = ErnParent.value),
          aColumnWith(name = aChildIdWithPrefix(VatId), value = VATChildUnitTypeAsString),
          aColumnWith(name = aChildIdWithPrefix(CHId), value = CHChildUnitTypeAsString))
      ).mkString("[", ",", "]")
    }}"""

  info("As a SBR user")
  info("I want to retrieve the Unit Link that matches the given unit id, unit type and specific period")
  info("Where I can than view Unit Links details on the user interface")

  feature("retrieve units links for an existing statistical unit") {
    scenario("by the exact statistical unit identifier, statistical unit type and period") { wsClient =>
      Given(s"a unit links exists with $TargetLeu, $TargetLegalUnitType and $TargetPeriod")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withStatUnit = TargetLeu.value, withUnitType = TargetLegalUnitType, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(UnitLinksSingleMatchHBaseResponseBody)
      ))

      When(s"a Unit Links is request with $TargetLeu, $TargetLegalUnitType and $TargetPeriod")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(TargetPeriod)}/types/${UnitType.toAcronym(TargetLegalUnitType)}/units/${TargetLeu.value}").get())

      Then(s"the details of the Unit Links identified with $TargetLeu, $TargetLegalUnitType and $TargetPeriod is returned")
      response.status shouldBe OK
      response.header(CONTENT_TYPE) shouldBe Some(JSON)
      response.json.as[UnitLinks] shouldBe
        UnitLinks(id = TargetLeu.value, period = TargetPeriod,
          parents = Some(Map(UnitType.toAcronym(EntParentUnitType) -> ErnParent.value)),
          children = Some(Map(VatId -> VATChildUnitTypeAsString, CHId -> CHChildUnitTypeAsString)),
          unitType = TargetLegalUnitType)
    }
  }

}
