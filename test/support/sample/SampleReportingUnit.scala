package support.sample

import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnit, Rurn }

trait SampleReportingUnit {

  val SampleEnterpriseLink = EnterpriseLink(
    ern = Ern("1000000001"),
    entref = Some("entref-123")
  )

  val SampleAddress = Address(
    line1 = "10 Long Road",
    line2 = Some("Newport"),
    line3 = Some("Gwent"),
    line4 = Some("South Wales"),
    line5 = Some("UK"),
    postcode = "NP10 8XG"
  )

  val SampleAllValuesReportingUnit = ReportingUnit(
    Rurn("33000000000"),
    ruref = Some("ruref-123"),
    enterprise = SampleEnterpriseLink,
    name = "Sample Company 1",
    tradingStyle = Some("A"),
    legalStatus = "legalStatus",
    address = SampleAddress,
    sic07 = "10000",
    employees = 100,
    employment = 500,
    turnover = 623481,
    prn = BigDecimal("0.2"),
    region = "E12000001"
  )

  val SampleAddress1 = Address(
    line1 = "11 Long Road",
    line2 = Some("Newport"),
    line3 = Some("Gwent"),
    line4 = Some("South Wales"),
    line5 = Some("UK"),
    postcode = "NP10 8XY"
  )

  val SampleAllValuesReportingUnit1 = ReportingUnit(
    Rurn("33000000001"),
    ruref = Some("ruref-124"),
    enterprise = SampleEnterpriseLink,
    name = "Sample Company 2",
    tradingStyle = Some("B"),
    legalStatus = "otherLegalStatus",
    address = SampleAddress1,
    sic07 = "20000",
    employees = 8462,
    employment = 193,
    turnover = 856288,
    prn = BigDecimal("0.3"),
    region = "E12000001"
  )

  private def withOnlyMandatoryFields(ru: ReportingUnit): ReportingUnit =
    ru.copy(
      ruref = None,
      enterprise = ru.enterprise.copy(entref = None),
      tradingStyle = None,
      address = ru.address.copy(line2 = None, line3 = None, line4 = None, line5 = None)
    )

  val SampleMandatoryValuesReportingUnit = withOnlyMandatoryFields(SampleAllValuesReportingUnit)

  def aReportingUnit(ern: Ern, rurn: Rurn, template: ReportingUnit = SampleAllValuesReportingUnit): ReportingUnit =
    template.copy(rurn = rurn, enterprise = template.enterprise.copy(ern = ern))
}
