package repository.hbase.reportingunit

import javax.inject.Inject

import com.typesafe.scalalogging.LazyLogging
import repository.RestRepository._
import repository.{ ReportingUnitRepository, RestRepository, RowMapper }
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnit, Rurn }

import scala.concurrent.Future

case class HBaseRestReportingUnitRepositoryConfig(tableName: String)

class HBaseRestReportingUnitRepository @Inject() (
    config: HBaseRestReportingUnitRepositoryConfig,
    restRepository: RestRepository,
    rowMapper: RowMapper[ReportingUnit]
) extends ReportingUnitRepository with LazyLogging {

  def retrieveReportingUnit(ern: Ern, period: Period, rurn: Rurn): Future[Either[ErrorMessage, Option[ReportingUnit]]] = ???
  def findReportingUnitsForEnterprise(ern: Ern, period: Period): Future[Either[ErrorMessage, Seq[ReportingUnit]]] = ???
}
