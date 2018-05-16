package repository.hbase.reportingunit

import javax.inject.Inject

import com.typesafe.scalalogging.LazyLogging
import repository.RestRepository._
import repository.hbase.HBase._
import repository.{ ReportingUnitRepository, RestRepository, RowMapper }
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnit, Rurn }
import utils.EitherSupport

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class HBaseRestReportingUnitRepositoryConfig(tableName: String)

class HBaseRestReportingUnitRepository @Inject() (
    config: HBaseRestReportingUnitRepositoryConfig,
    restRepository: RestRepository,
    rowMapper: RowMapper[ReportingUnit]
) extends ReportingUnitRepository with LazyLogging {

  def retrieveReportingUnit(ern: Ern, period: Period, rurn: Rurn): Future[Either[ErrorMessage, Option[ReportingUnit]]] = {
    logger.info(s"Retrieving Reporting Unit with [$ern] [$rurn] for [$period].")
    restRepository.findRow(config.tableName, ReportingUnitQuery.byRowKey(ern, period, rurn), DefaultColumnFamily).map(fromErrorOrRow)
  }

  def findReportingUnitsForEnterprise(ern: Ern, period: Period): Future[Either[ErrorMessage, Seq[ReportingUnit]]] = {
    logger.info(s"Finding Reporting Units with [$ern] for [$period].")
    restRepository.findRows(config.tableName, ReportingUnitQuery.forAllWith(ern, period), DefaultColumnFamily).map(fromErrorOrRows)
  }

  private def fromErrorOrRow(errorOrRow: Either[ErrorMessage, Option[Row]]): Either[ErrorMessage, Option[ReportingUnit]] = {
    val errorOrRows = errorOrRow.right.map(_.toSeq)
    fromErrorOrRows(errorOrRows).right.map { reportingUnits =>
      require(reportingUnits.size <= 1)
      reportingUnits.headOption
    }
  }

  /*
 * Note that we have made the decision to fail the entire search if ANY of the rows returned from HBase cannot
 * successfully construct a valid Reporting Unit.
 */
  private def fromErrorOrRows(errorOrRows: Either[ErrorMessage, Seq[Row]]): Either[ErrorMessage, Seq[ReportingUnit]] = {
    logger.debug(s"Reporting Unit response is [$errorOrRows].")
    errorOrRows.right.flatMap { rows =>
      val errorOrReportingUnits = EitherSupport.sequence(rows.map(fromRow))
      logger.debug(s"From rows to Reporting Units conversion result is [$errorOrReportingUnits].")
      errorOrReportingUnits
    }
  }

  private def fromRow(row: Row): Either[ErrorMessage, ReportingUnit] = {
    val optReportingUnit = rowMapper.fromRow(row)
    if (optReportingUnit.isEmpty) logger.warn(s"Unable to construct a Reporting Unit from HBase [${config.tableName}] row [$row].")
    optReportingUnit.toRight("Unable to construct a Reporting Unit from Row data")
  }
}
