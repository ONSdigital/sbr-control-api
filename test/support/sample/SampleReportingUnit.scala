package support.sample

import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnit, Rurn }

trait SampleReportingUnit {

  val SampleAllValuesReportingUnit = ReportingUnit(
    Rurn("33000000000"),
    ruref = Some("ruref-123"),
    ern = Ern("1000000001"),
    entref = Some("entref-123"),
    name = "Sample Company 1",
    tradingStyle = Some("A"),
    legalStatus = Some("A"),
    address1 = "10 Long Road",
    address2 = Some("Newport"),
    address3 = Some("Gwent"),
    address4 = Some("South Wales"),
    address5 = Some("UK"),
    postcode = "NP10 8XG",
    sic07 = "10000",
    employees = 100,
    employment = 500,
    turnover = 623481,
    prn = "0.2"
  )

  val SampleAllValuesReportingUnit1 = ReportingUnit(
    Rurn("33000000001"),
    ruref = Some("ruref-124"),
    ern = Ern("1000000001"),
    entref = Some("entref-123"),
    name = "Sample Company 2",
    tradingStyle = Some("B"),
    legalStatus = Some("B"),
    address1 = "11 Long Road",
    address2 = Some("Newport"),
    address3 = Some("Gwent"),
    address4 = Some("South Wales"),
    address5 = Some("UK"),
    postcode = "NP10 8XY",
    sic07 = "20000",
    employees = 8462,
    employment = 193,
    turnover = 856288,
    prn = "0.3"
  )

  def withOnlyMandatoryFields(ru: ReportingUnit): ReportingUnit = ru.copy(ruref = None, entref = None,
    tradingStyle = None, legalStatus = None, address2 = None, address3 = None, address4 = None, address5 = None)

  val SampleMandatoryValuesReportingUnit = withOnlyMandatoryFields(SampleAllValuesReportingUnit)

  val SampleMandatoryValuesReportingUnit1 = withOnlyMandatoryFields(SampleAllValuesReportingUnit1)

  def aReportingUnit(ern: Ern, rurn: Rurn, template: ReportingUnit = SampleAllValuesReportingUnit): ReportingUnit =
    template.copy(rurn = rurn, ern = ern)
}
