package support.sample

import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.legalunit.{ Crn, LegalUnit, Ubrn, Uprn }

trait SampleLegalUnit {
  val SampleMandatoryValuesAddress = Address(line1 = "line1-value", line2 = None, line3 = None,
    line4 = None, line5 = None, postcode = "postcode-value")
  val SampleAllValuesAddress = SampleMandatoryValuesAddress.copy(
    line2 = Some("line2-value"), line3 = Some("line3-value"), line4 = Some("line4-value"), line5 = Some("line5-value")
  )

  val SampleMandatoryValuesLegalUnit = LegalUnit(ubrn = Ubrn("1111111100000000"), name = "name-value",
    legalStatus = "legalStatus-value", tradingStatus = None, tradingStyle = None, sic07 = "sic07-value",
    turnover = None, payeJobs = None, address = SampleMandatoryValuesAddress, birthDate = "01/01/2017",
    deathDate = None, deathCode = None, crn = None, uprn = None)
  val SampleAllValuesLegalUnit = SampleMandatoryValuesLegalUnit.copy(
    tradingStatus = Some("tradingStatus-value"),
    tradingStyle = Some("tradingStyle-value"), turnover = Some(5), payeJobs = Some(10), address = SampleAllValuesAddress,
    deathDate = Some("13/07/2018"), deathCode = Some("1"), crn = Some(Crn("01245670")), uprn = Some(Uprn("10023450178"))
  )

  def aLegalUnit(ubrn: Ubrn, template: LegalUnit = SampleAllValuesLegalUnit): LegalUnit =
    template.copy(ubrn = ubrn)
}
