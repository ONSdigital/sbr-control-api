import java.time.Month.MARCH

import fixture.ReadsUnitLinks._
import fixture.ServerAcceptanceSpec
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status.{NOT_FOUND, OK}
import play.mvc.Http.MimeTypes.JSON
import repository.hbase.unitlinks.{HBaseRestUnitLinksRepository, UnitLinksQualifier, UnitLinksRowKey}
import support.WithWireMockHBase
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.unitlinks._

class UnitLinksAcceptanceSpec extends ServerAcceptanceSpec with WithWireMockHBase {

  private val TargetLeuUnitId = UnitId("1000000000000012")
  private val TargetPeriod = Period.fromYearMonth(2018, MARCH)
  private val TargetLegalUnitType = UnitType.LegalUnit

  private val ErnParent = Ern("1000000123")
  private val VatId = UnitId("401347263289")
  private val CHId = UnitId("01752564")
  private val EntParentUnitType = UnitType.Enterprise
  private val vatUnitType = UnitType.CompaniesHouse
  private val chUnitType = UnitType.CompaniesHouse
  private val CHChildUnitTypeAsString = UnitType.toAcronym(chUnitType)
  private val VATChildUnitTypeAsString = UnitType.toAcronym(vatUnitType)
  private val Family = HBaseRestUnitLinksRepository.ColumnFamily

  private val UnitLinksSingleMatchHBaseResponseBody =
    s"""{ "Row": ${
      List(
        aRowWith(key = s"${UnitLinksRowKey(TargetLeuUnitId, TargetLegalUnitType)}", columns =
          aColumnWith(Family, qualifier = UnitLinksQualifier.toParent(EntParentUnitType), value = ErnParent.value),
          aColumnWith(Family, qualifier = UnitLinksQualifier.toChild(VatId), value = VATChildUnitTypeAsString),
          aColumnWith(Family, qualifier = UnitLinksQualifier.toChild(CHId), value = CHChildUnitTypeAsString))
      ).mkString("[", ",", "]")
    }}"""

  info("As a SBR user")
  info("I want to retrieve the Unit Link that matches the given unit id, unit type and specific period")
  info("So that I can than view Unit Link details via the user interface")

  feature("retrieve units links for an existing statistical unit") {
    scenario("by the exact statistical unit identifier, statistical unit type and period") { wsClient =>
      Given(s"a unit links exists with $TargetLeuUnitId, $TargetLegalUnitType and $TargetPeriod")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLeuUnitId, withUnitType = TargetLegalUnitType, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(UnitLinksSingleMatchHBaseResponseBody)
      ))

      When(s"the Unit Links with $TargetLeuUnitId, $TargetLegalUnitType and $TargetPeriod are requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(TargetPeriod)}/types/${UnitType.toAcronym(TargetLegalUnitType)}/units/${TargetLeuUnitId.value}").get())

      Then(s"the details of the Unit Links identified by $TargetLeuUnitId, $TargetLegalUnitType and $TargetPeriod are returned")
      response.status shouldBe OK
      response.header(CONTENT_TYPE) shouldBe Some(JSON)
      response.json.as[UnitLinks] shouldBe
        UnitLinks(id = TargetLeuUnitId, period = TargetPeriod,
          parents = Some(Map(EntParentUnitType -> UnitId(ErnParent.value))),
          children = Some(Map(VatId -> vatUnitType, CHId -> chUnitType)),
          unitType = TargetLegalUnitType)
    }
  }

  feature("respond to a request to retrieve unit links that do not exist") {
    scenario("by the exact statistical unit identifier, statistical unit type and period") { wsClient =>
      Given(s"unit links do not exist with $TargetLeuUnitId, $TargetLegalUnitType and $TargetPeriod")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetLeuUnitId, withUnitType = TargetLegalUnitType, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(NoMatchFoundResponse)
      ))

      When(s"the Unit links with $TargetLeuUnitId, $TargetLegalUnitType and $TargetPeriod are requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(TargetPeriod)}/types/${UnitType.toAcronym(TargetLegalUnitType)}/units/${TargetLeuUnitId.value}").get())

      Then("a NOT_FOUND response is returned")
      response.status shouldBe NOT_FOUND
    }
  }
}
