package repository

import repository.RestRepository.ErrorMessage
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnit, Rurn }

import scala.concurrent.Future

trait ReportingUnitRepository {
  def retrieveReportingUnit(ern: Ern, period: Period, rurn: Rurn): Future[Either[ErrorMessage, Option[ReportingUnit]]]
  def findReportingUnitsForEnterprise(ern: Ern, period: Period): Future[Either[ErrorMessage, Seq[ReportingUnit]]]
}
