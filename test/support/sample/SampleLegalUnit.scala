package support.sample

import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.legalunit.{ LegalUnit, UBRN }

trait SampleLegalUnit {
  val SampleMandatoryValuesEnterpriseLink = EnterpriseLink(Ern("1000000012"), entref = None)
  val SampleAllValuesEnterpriseLink = SampleMandatoryValuesEnterpriseLink.copy(entref = Some("entref-value"))

  val SampleMandatoryValuesLegalUnit = LegalUnit(UBRN("1111111100000000"), UBRNref = None, SampleMandatoryValuesEnterpriseLink)

  val SampleAllValuesLegalUnit = SampleMandatoryValuesLegalUnit.copy(
    UBRNref = Some("luref-value")
  )

  def anEnterpriseLink(ern: Ern, template: EnterpriseLink = SampleAllValuesEnterpriseLink): EnterpriseLink =
    template.copy(ern = ern)

  def aLegalUnit(ern: Ern, uBRN: UBRN, template: LegalUnit = SampleAllValuesLegalUnit): LegalUnit =
    template.copy(uBRN = uBRN, enterprise = template.enterprise.copy(ern = ern))
}
