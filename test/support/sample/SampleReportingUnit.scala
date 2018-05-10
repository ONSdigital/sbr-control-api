package support.sample

import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnit, Rurn }

trait SampleReportingUnit {
  val SampleMandatoryValuesReportingUnit = ReportingUnit(Rurn("900000012"), ruref = None)

  val SampleAllValuesReportingUnit = SampleMandatoryValuesReportingUnit.copy(ruref = Some("luref-value"))

  def aReportingUnit(ern: Ern, rurn: Rurn, template: ReportingUnit = SampleAllValuesReportingUnit): ReportingUnit =
    template.copy(rurn = rurn)
}
