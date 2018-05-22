package support.sample

import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern }

trait SampleEnterpriseUnit {

  val SampleEnterpriseId = Ern("1000000012")
  val SampleNumberOfEmployees = 100
  val SampleJobs = 15
  val SampleEnterpriseReference = "someEnterpriseRef"
  val SampleEnterpriseName = "Company Name Plc"
  val SamplePostcode = "NP0 XXX"
  val SampleLegalStatus = "some-LegalUnit"

  val SampleAddressLine1 = "addressLine-1"
  val SampleAddressLine2 = "addressLine-2"
  val SampleAddressLine3 = "addressLine-3"
  val SampleAddressLine4 = "addressLine-4"
  val SampleAddressLine5 = "addressLine-5"

  val SampleSIC07 = "sic07"
  val SampleTradingStyle = "trading_style"
  val SampleContainedTurnover = 99
  val SampleStandardTurnover = 99
  val SampleGroupTurnover = 99
  val SampleApportionedTurnover = 99
  val SampleEnterpriseTurnover = 99

  val SampleFullAddress: Address = Address(line1 = SampleAddressLine1, line2 = Some(SampleAddressLine2),
    line3 = Some(SampleAddressLine3), line4 = Some(SampleAddressLine4), line5 = Some(SampleAddressLine5),
    postcode = SamplePostcode)

  val SamplePartialAddress: Address = Address(line1 = SampleAddressLine1, line2 = None, line3 = None, line4 = None,
    line5 = None, postcode = SamplePostcode)

  val SampleEnterpriseWithAllFields: Enterprise =
    Enterprise(SampleEnterpriseId, entref = Some(SampleEnterpriseReference), name = SampleEnterpriseName,
      tradingStyle = Some(SampleTradingStyle), address = SampleFullAddress, sic07 = SampleSIC07,
      legalStatus = SampleLegalStatus, employees = Some(SampleNumberOfEmployees), jobs = Some(SampleJobs),
      containedTurnover = Some(SampleContainedTurnover), standardTurnover = Some(SampleStandardTurnover),
      groupTurnover = Some(SampleGroupTurnover), apportionedTurnover = Some(SampleApportionedTurnover),
      enterpriseTurnover = Some(SampleEnterpriseTurnover))

  val SampleEnterpriseWithNoOptionalFields: Enterprise =
    Enterprise(SampleEnterpriseId, entref = None, name = SampleEnterpriseName,
      tradingStyle = None, address = SamplePartialAddress, sic07 = SampleSIC07,
      legalStatus = SampleLegalStatus, employees = None, jobs = None, containedTurnover = None,
      standardTurnover = None, groupTurnover = None,
      apportionedTurnover = None, enterpriseTurnover = None)

  def aEnterpriseSample(ern: Ern, apportionedTurnover: Option[Int] = None, template: Enterprise = SampleEnterpriseWithAllFields,
    address: Address = SampleFullAddress): Enterprise =
    template.copy(ern = ern, apportionedTurnover = apportionedTurnover, address = address)

  def aAddressSample(line2: Option[String] = None, line3: Option[String] = None, line4: Option[String] = None,
    line5: Option[String] = None): Address =
    SamplePartialAddress.copy(line2 = line2, line3 = line3, line4 = line4, line5 = line5)

}
