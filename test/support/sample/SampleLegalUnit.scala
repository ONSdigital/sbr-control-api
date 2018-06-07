package support.sample

import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.legalunit.{ LegalUnit, UBRN }
import uk.gov.ons.sbr.models.Address

trait SampleLegalUnit {
  val SampleMandatoryValuesAddress = Address(line1 = "line1-value", line2 = None, line3 = None,
    line4 = None, line5 = None, postcode = "postcode-value")
  val SampleAllValuesAddress = SampleMandatoryValuesAddress.copy(
    line2 = Some("line2-value"),
    line3 = Some("line3-value"), line4 = Some("line4-value"), line5 = Some("line5-value")
  )
  val SampleMandatoryValuesEnterpriseLink = EnterpriseLink(Ern("1000000012"), entref = None)
  val SampleAllValuesEnterpriseLink = SampleMandatoryValuesEnterpriseLink.copy(entref = Some("entref-value"))

  val SampleMandatoryValuesLegalUnit = LegalUnit(UBRN("1111111100000000"), crn = None, name = "name-value",
    legalStatus = "legalStatus-value", tradingStatus = "tradingStatus-value", tradingstyle = None, sic07 = "sic07-value",
    turnover = None, jobs = None, SampleMandatoryValuesEnterpriseLink, SampleMandatoryValuesAddress)

  val SampleAllValuesLegalUnit = SampleMandatoryValuesLegalUnit.copy(UBRN("1111111100000000"), crn = Some("crn-value"), name = "name-value",
    legalStatus = "legalStatus-value", tradingStatus = "tradingStatus-value", tradingstyle = Some("tradingstyle-value"), sic07 = "sic07-value",
    turnover = Some(5), jobs = Some(10), SampleAllValuesEnterpriseLink, SampleAllValuesAddress)

  def anEnterpriseLink(ern: Ern, template: EnterpriseLink = SampleAllValuesEnterpriseLink): EnterpriseLink =
    template.copy(ern = ern)

  def aLegalUnit(ern: Ern, ubrn: UBRN, template: LegalUnit = SampleAllValuesLegalUnit): LegalUnit =
    template.copy(ubrn = ubrn, enterprise = template.enterprise.copy(ern = ern))
}
