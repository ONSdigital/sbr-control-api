import java.time.Month.MARCH

import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status.{ NOT_FOUND, OK }
import play.mvc.Http.MimeTypes.JSON

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.unitlinks._

import fixture.ServerAcceptanceSpec
import fixture.ReadsUnitLinks._
import repository.hbase.unitlinks.UnitLinksRowKey
import support.WithWireMockHBase

class UnitLinksAcceptanceSpec extends ServerAcceptanceSpec with WithWireMockHBase {

  private val TargetLeuUnitId = UnitId("1000000012")
  private val TargetPeriod = Period.fromYearMonth(2018, MARCH)
  private val TargetLegalUnitType = UnitType.LegalUnit

  private val ErnParent = Ern("1000000123")
  private val VatId = "401347263289"
  private val CHId = "01752564"
  private val EntParentUnitType = UnitType.Enterprise
  private val vatUnitType = UnitType.CompaniesHouse
  private val chUnitType = UnitType.CompaniesHouse
  private val CHChildUnitTypeAsString = UnitType.toAcronym(chUnitType)
  private val VATChildUnitTypeAsString = UnitType.toAcronym(vatUnitType)

  private val UnitLinksSingleMatchHBaseResponseBody =
    s"""{ "Row": ${
      List(
        aRowWith(key = s"${UnitLinksRowKey(TargetLeuUnitId, TargetLegalUnitType, TargetPeriod)}", columns =
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
      Given(s"a unit links exists with $TargetLeuUnitId, $TargetLegalUnitType and $TargetPeriod")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withStatUnit = TargetLeuUnitId, withUnitType = TargetLegalUnitType, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(UnitLinksSingleMatchHBaseResponseBody)
      ))

      When(s"a Unit Links is request with $TargetLeuUnitId, $TargetLegalUnitType and $TargetPeriod")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(TargetPeriod)}/types/${UnitType.toAcronym(TargetLegalUnitType)}/units/${TargetLeuUnitId.value}").get())

      Then(s"the details of the Unit Links identified with $TargetLeuUnitId, $TargetLegalUnitType and $TargetPeriod is returned")
      response.status shouldBe OK
      response.header(CONTENT_TYPE) shouldBe Some(JSON)
      response.json.as[UnitLinks] shouldBe
        UnitLinks(id = TargetLeuUnitId, period = TargetPeriod,
          parents = Some(Map(EntParentUnitType -> UnitId(ErnParent.value))),
          children = Some(Map(UnitId(VatId) -> vatUnitType, UnitId(CHId) -> chUnitType)),
          unitType = TargetLegalUnitType)
    }
  }

  feature("respond to a non-existent Enterprise Unit request") {
    scenario("by an exact Enterprise reference (ERN) and period") { wsClient =>
      Given(s"an does not exist with $TargetLeuUnitId, $TargetLegalUnitType and $TargetPeriod")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withStatUnit = TargetLeuUnitId, withUnitType = TargetLegalUnitType, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(NoMatchFoundResponse)
      ))

      When(s"an unit links with $TargetLeuUnitId, $TargetLegalUnitType and $TargetPeriod is requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(TargetPeriod)}/types/${UnitType.toAcronym(TargetLegalUnitType)}/units/${TargetLeuUnitId.value}").get())

      Then("a NOT_FOUND response status to given back with no details are returned")
      response.status shouldBe NOT_FOUND
    }
  }

}
