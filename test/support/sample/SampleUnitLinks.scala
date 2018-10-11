package support.sample

import java.time.Month.AUGUST

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks._

trait SampleUnitLinks {

  val CompaniesHouse = UnitType.toAcronym(UnitType.CompaniesHouse)
  val ValueAddedTax = UnitType.toAcronym(UnitType.ValueAddedTax)
  val PayAsYouEarnTax = UnitType.toAcronym(UnitType.PayAsYouEarn)
  val Enterprise = UnitType.toAcronym(UnitType.Enterprise)
  val LegalUnit = UnitType.toAcronym(UnitType.LegalUnit)
  val LocalUnit = UnitType.toAcronym(UnitType.LocalUnit)
  val ReportingUnit = UnitType.toAcronym(UnitType.ReportingUnit)

  val SampleCompaniesHouseChildId = "NI034159"
  val SamplePayAsYouEarnChildId = "2738768"
  val SampleEnterpriseParentId = "9900156115"

  val SampleUnitId = UnitId("12345678910")
  val SamplePeriod: Period = Period.fromYearMonth(2018, AUGUST)
  val SampleUnitType: UnitType = UnitType.fromAcronym(LegalUnit)
  val SampleChildren: Map[UnitId, UnitType] =
    Map(
      UnitId(SamplePayAsYouEarnChildId) -> UnitType.fromAcronym(PayAsYouEarnTax),
      UnitId(SampleCompaniesHouseChildId) -> UnitType.fromAcronym(CompaniesHouse)
    )
  val SampleParents: Map[UnitType, UnitId] =
    Map(UnitType.fromAcronym(Enterprise) -> UnitId(SampleEnterpriseParentId))

  val SampleUnitLinksNoPeriodWithAllFields = UnitLinksNoPeriod(
    id = SampleUnitId, unitType = SampleUnitType, parents = Some(SampleParents), children = Some(SampleChildren)
  )

  val SampleUnitLinksWithOnlyMandatoryFields: UnitLinks =
    UnitLinks(id = SampleUnitId, period = SamplePeriod, parents = None, children = None, unitType = SampleUnitType)

  val SampleUnitLinksWithAllFields: UnitLinks =
    SampleUnitLinksWithOnlyMandatoryFields.copy(parents = Some(SampleParents), children = Some(SampleChildren))

  // TODO - USE template as the default values
  def aUnitLinksSample(unitId: UnitId = SampleUnitId, parents: Option[Map[UnitType, UnitId]] = Some(SampleParents),
    children: Option[Map[UnitId, UnitType]] = Some(SampleChildren),
    template: UnitLinks = SampleUnitLinksWithOnlyMandatoryFields): UnitLinks =
    template.copy(id = unitId, parents = parents, children = children)

  def unitIdAsString(unitId: UnitId): String = unitId.value
}
