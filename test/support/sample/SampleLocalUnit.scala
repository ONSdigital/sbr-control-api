package support.sample

import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.localunit.{ LocalUnit, Lurn }

trait SampleLocalUnit {
  val SampleMandatoryValuesAddress = Address(line1 = "line1-value", line2 = None, line3 = None,
    line4 = None, line5 = None, postcode = "postcode-value")
  val SampleAllValuesAddress = SampleMandatoryValuesAddress.copy(
    line2 = Some("line2-value"),
    line3 = Some("line3-value"), line4 = Some("line4-value"), line5 = Some("line5-value")
  )

  val SampleMandatoryValuesEnterpriseLink = EnterpriseLink(Ern("1000000012"), entref = None)
  val SampleAllValuesEnterpriseLink = SampleMandatoryValuesEnterpriseLink.copy(entref = Some("entref-value"))

  val SampleMandatoryValuesLocalUnit = LocalUnit(Lurn("900000011"), luref = None, name = "COMPANY X",
    tradingStyle = None, sic07 = "sic07-value", employees = 42,
    SampleMandatoryValuesEnterpriseLink, SampleMandatoryValuesAddress)

  val SampleAllValuesLocalUnit = SampleMandatoryValuesLocalUnit.copy(
    luref = Some("luref-value"),
    tradingStyle = Some("tradingStyle-value"),
    enterprise = SampleAllValuesEnterpriseLink,
    address = SampleAllValuesAddress
  )

  def anEnterpriseLink(ern: Ern, template: EnterpriseLink = SampleAllValuesEnterpriseLink): EnterpriseLink =
    template.copy(ern = ern)

  def aLocalUnit(ern: Ern, lurn: Lurn, template: LocalUnit = SampleAllValuesLocalUnit): LocalUnit =
    template.copy(lurn = lurn, enterprise = template.enterprise.copy(ern = ern))
}
