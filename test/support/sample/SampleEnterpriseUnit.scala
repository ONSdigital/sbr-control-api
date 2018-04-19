package support.sample

import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern }

trait SampleEnterpriseUnit {

  val SampleEnterpriseId = Ern("1000000012")
  val SampleNumberOfEmployees = 100
  val SampleJobs = 15
  val SampleEnterpriseReference = "someEnterpriseRef"
  val SampleEnterpriseName = "Company Name Plc"
  val SamplePostcode = "NP0 XXX"
  val SampleLegalStatus = "some-LegalUnit"

  val SampleEnterpriseWithAllFields: Enterprise =
    Enterprise(SampleEnterpriseId, entref = SampleEnterpriseReference, name = SampleEnterpriseName, postcode = SamplePostcode,
      legalStatus = SampleLegalStatus, employees = Some(SampleNumberOfEmployees), jobs = Some(SampleJobs))

  val SampleEnterpriseWithNoOptionalFields: Enterprise =
    Enterprise(SampleEnterpriseId, entref = SampleEnterpriseReference, name = SampleEnterpriseName, postcode = SamplePostcode,
      legalStatus = SampleLegalStatus, employees = None, jobs = None)

  def aEnterpriseSample(ern: Ern, template: Enterprise = SampleEnterpriseWithAllFields): Enterprise =
    template.copy(ern = ern)

}
