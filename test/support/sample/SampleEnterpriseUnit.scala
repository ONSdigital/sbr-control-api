package support.sample

import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern, Imputed, Turnover }

trait SampleEnterpriseUnit {

  val SampleEnterpriseId = Ern("1000000012")
  val SampleNumberOfEmployees = 100
  val SampleJobs = 15
  val SampleEnterpriseReference = "someEnterpriseRef"
  val SampleEnterpriseName = "Company Name Plc"
  val SamplePostcode = "NP0 XXX"
  val SampleLegalStatus = "some-LegalStatus"
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
  val SamplePrn = BigDecimal("0.016587362")
  val SampleWorkingProprietors = 1
  val SampleEmployment = 101
  val SampleRegion = "E12000001"
  val SampleImputedEmployees = 10
  val SampleImputedTurnover = 5000

  val SampleFullAddress: Address = Address(line1 = SampleAddressLine1, line2 = Some(SampleAddressLine2),
    line3 = Some(SampleAddressLine3), line4 = Some(SampleAddressLine4), line5 = Some(SampleAddressLine5),
    postcode = SamplePostcode)

  val SamplePartialAddress: Address = Address(line1 = SampleAddressLine1, line2 = None, line3 = None, line4 = None,
    line5 = None, postcode = SamplePostcode)

  val SampleTurnoverWithAllFields = Turnover(
    containedTurnover = Some(SampleContainedTurnover),
    standardTurnover = Some(SampleStandardTurnover),
    groupTurnover = Some(SampleGroupTurnover),
    apportionedTurnover = Some(SampleApportionedTurnover),
    enterpriseTurnover = Some(SampleEnterpriseTurnover)
  )

  val SampleImputedWithAllFields = Imputed(
    employees = Some(SampleImputedEmployees),
    turnover = Some(SampleImputedTurnover)
  )

  val SampleEnterpriseWithAllFields: Enterprise =
    Enterprise(
      SampleEnterpriseId,
      entref = Some(SampleEnterpriseReference),
      name = SampleEnterpriseName,
      tradingStyle = Some(SampleTradingStyle),
      address = SampleFullAddress,
      sic07 = SampleSIC07,
      legalStatus = SampleLegalStatus,
      employees = Some(SampleNumberOfEmployees),
      jobs = Some(SampleJobs),
      turnover = Some(SampleTurnoverWithAllFields),
      prn = SamplePrn,
      workingProprietors = SampleWorkingProprietors,
      employment = SampleEmployment,
      region = SampleRegion,
      imputed = Some(SampleImputedWithAllFields)
    )

  val SampleEnterpriseWithNoOptionalFields: Enterprise =
    SampleEnterpriseWithAllFields.copy(entref = None, tradingStyle = None, address = SamplePartialAddress,
      employees = None, jobs = None, turnover = None, imputed = None)

  def aEnterpriseSample(ern: Ern, turnover: Option[Turnover] = Some(SampleTurnoverWithAllFields),
    template: Enterprise = SampleEnterpriseWithAllFields, address: Address = SampleFullAddress): Enterprise =
    template.copy(ern = ern, turnover = turnover, address = address)

  def aAddressSampleWithOptionalValues(line2: Option[String] = None, line3: Option[String] = None,
    line4: Option[String] = None, line5: Option[String] = None): Address =
    SamplePartialAddress.copy(line2 = line2, line3 = line3, line4 = line4, line5 = line5)
}
