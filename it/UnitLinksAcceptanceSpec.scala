import java.time.Month.MARCH

import fixture.AbstractServerAcceptanceSpec
import fixture.ReadsUnitLinks._
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status.{NOT_FOUND, OK}
import play.mvc.Http.MimeTypes.JSON
import repository.hbase.unitlinks.{HBaseRestUnitLinksRepository, UnitLinksQualifier, UnitLinksRowKey}
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.UnitType.{CompaniesHouse, Enterprise, LegalUnit, ValueAddedTax, toAcronym}
import uk.gov.ons.sbr.models.unitlinks._

class UnitLinksAcceptanceSpec extends AbstractServerAcceptanceSpec {

  private val TargetUBRN = UnitId("1000000000000012")
  private val TargetPeriod = Period.fromYearMonth(2018, MARCH)
  private val LegalUnitAcronym = toAcronym(LegalUnit)
  private val Family = HBaseRestUnitLinksRepository.ColumnFamily
  private val EnterpriseUnitId = UnitId("1000000123")
  private val VatUnitId = UnitId("401347263289")
  private val CompaniesHouseUnitId = UnitId("01752564")
  private val EditedFlag = aColumnWith(Family, qualifier = "edited", value = "Y")
  private val RelatedUnitColumns = Seq(
    aColumnWith(Family, qualifier = UnitLinksQualifier.toParent(Enterprise), value = EnterpriseUnitId.value),
    aColumnWith(Family, qualifier = UnitLinksQualifier.toChild(VatUnitId), value = toAcronym(ValueAddedTax)),
    aColumnWith(Family, qualifier = UnitLinksQualifier.toChild(CompaniesHouseUnitId), value = toAcronym(CompaniesHouse))
  )

  private val UnitLinksForTargetUbrnWithNoClericalEditsHBaseResponseBody =
    unitLinksForTargetUbrnHBaseResponseBody(columns = RelatedUnitColumns)

  private val UnitLinksForTargetUbrnWithClericalEditsHBaseResponseBody =
    unitLinksForTargetUbrnHBaseResponseBody(EditedFlag +: RelatedUnitColumns)

  private def unitLinksForTargetUbrnHBaseResponseBody(columns: Seq[String]): String =
    s"""{ "Row": ${
      List(
        aRowWith(key = s"${UnitLinksRowKey(TargetUBRN, LegalUnit)}", columns: _*)
      ).mkString("[", ",", "]")
    }}"""

  info("As a SBR user")
  info("I want to retrieve the Unit Link that matches the given unit id, unit type and specific period")
  info("So that I can than view Unit Link details via the user interface")

  feature("retrieve units links for a statistical unit") {
    scenario("when the unit does not have clerically edited links") { wsClient =>
      Given(s"unit links exist from the Legal Unit identified by $TargetUBRN for $TargetPeriod (that have not been clerically edited)")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetUBRN, withUnitType = LegalUnit, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(UnitLinksForTargetUbrnWithNoClericalEditsHBaseResponseBody)
      ))

      When(s"the unit links from the Legal Unit identified by $TargetUBRN for $TargetPeriod are requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(TargetPeriod)}/types/$LegalUnitAcronym/units/${TargetUBRN.value}").get())

      Then(s"the details of the unit links from the Legal Unit identified by $TargetUBRN for $TargetPeriod are returned")
      response.status shouldBe OK
      response.header(CONTENT_TYPE) shouldBe Some(JSON)
      response.json.as[UnitLinks] shouldBe
        UnitLinks(
          id = TargetUBRN,
          unitType = LegalUnit,
          period = TargetPeriod,
          parents = Some(Map(Enterprise -> EnterpriseUnitId)),
          children = Some(Map(
            VatUnitId -> ValueAddedTax,
            CompaniesHouseUnitId -> CompaniesHouse)))
    }

    /*
     * Note that we do not currently expose edited status / history as part of the "unit links model".
     */
    scenario("when the unit has clerically edited links") { wsClient =>
      Given(s"unit links exist from the Legal Unit identified by $TargetUBRN for $TargetPeriod (that have been clerically edited)")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetUBRN, withUnitType = LegalUnit, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(UnitLinksForTargetUbrnWithClericalEditsHBaseResponseBody)
      ))

      When(s"the unit links from the Legal Unit identified by $TargetUBRN for $TargetPeriod are requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(TargetPeriod)}/types/$LegalUnitAcronym/units/${TargetUBRN.value}").get())

      Then(s"the details of the unit links from the Legal Unit identified by $TargetUBRN for $TargetPeriod are returned")
      response.status shouldBe OK
      response.header(CONTENT_TYPE) shouldBe Some(JSON)
      response.json.as[UnitLinks] shouldBe
        UnitLinks(
          id = TargetUBRN,
          unitType = LegalUnit,
          period = TargetPeriod,
          parents = Some(Map(Enterprise -> EnterpriseUnitId)),
          children = Some(Map(
            VatUnitId -> ValueAddedTax,
            CompaniesHouseUnitId -> CompaniesHouse)))
    }

    scenario("when the unit does not exist") { wsClient =>
      Given(s"unit links do not exist for a Legal Unit identified by $TargetUBRN for $TargetPeriod")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetUBRN, withUnitType = LegalUnit, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(NoMatchFoundResponse)
      ))

      When(s"the unit links from a Legal Unit identified by $TargetUBRN for $TargetPeriod are requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(TargetPeriod)}/types/$LegalUnitAcronym/units/${TargetUBRN.value}").get())

      Then("a NOT_FOUND response is returned")
      response.status shouldBe NOT_FOUND
    }

    scenario("when the unit has no links (an 'orphaned unit')") { wsClient =>
      Given(s"neither parent or child links exist from a Legal Unit identified by $TargetUBRN for $TargetPeriod (which has been clerically edited)")
      stubHBaseFor(aUnitLinksExactRowKeyRequest(withUnitId = TargetUBRN, withUnitType = LegalUnit, withPeriod = TargetPeriod).willReturn(
        anOkResponse().withBody(unitLinksForTargetUbrnHBaseResponseBody(columns = Seq(EditedFlag)))
      ))

      When(s"the unit links from a Legal Unit identified by $TargetUBRN for $TargetPeriod are requested")
      val response = await(wsClient.url(s"/v1/periods/${Period.asString(TargetPeriod)}/types/$LegalUnitAcronym/units/${TargetUBRN.value}").get())

      Then("the response should contain neither parents or children")
      response.status shouldBe OK
      response.header(CONTENT_TYPE) shouldBe Some(JSON)
      response.json.as[UnitLinks] shouldBe
        UnitLinks(
          id = TargetUBRN,
          unitType = LegalUnit,
          period = TargetPeriod,
          parents = None,
          children = None)
    }
  }
}
