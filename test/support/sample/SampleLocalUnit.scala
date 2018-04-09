package support.sample

import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.localunit.{ Address, LocalUnit, Lurn }

trait SampleLocalUnit {
  val SampleMandatoryValuesAddress = Address(line1 = "line1-value", line2 = "line2-value", line3 = "line3-value",
    line4 = "line4-value", line5 = "line5-value", postcode = "postcode-value")
  val SampleAllValuesAddress = SampleMandatoryValuesAddress.copy()

  val SampleMandatoryValuesEnterpriseLink = EnterpriseLink(Ern("1000000012"), entref = None)
  val SampleAllValuesEnterpriseLink = SampleMandatoryValuesEnterpriseLink.copy(entref = Some("entref-value"))

  val SampleMandatoryValuesLocalUnit = LocalUnit(Lurn("900000011"), luref = "luref-value",
    name = "COMPANY X", tradingStyle = "tradingStyle-value", sic07 = "sic07-value", employees = 42,
    SampleMandatoryValuesEnterpriseLink, SampleMandatoryValuesAddress)
  val SampleAllValuesLocalUnit = SampleMandatoryValuesLocalUnit.copy(
    enterprise = SampleAllValuesEnterpriseLink,
    address = SampleAllValuesAddress
  )

  def anEnterpriseLink(ern: Ern, template: EnterpriseLink = SampleAllValuesEnterpriseLink): EnterpriseLink =
    template.copy(ern = ern)

  def aLocalUnit(ern: Ern, lurn: Lurn, template: LocalUnit = SampleAllValuesLocalUnit): LocalUnit =
    template.copy(lurn = lurn, enterprise = template.enterprise.copy(ern = ern))
}
