package repository.hbase.legalunit

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import com.typesafe.scalalogging.LazyLogging
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.{LegalUnit, Ubrn}
import utils.EitherSupport
import repository.RestRepository.{ErrorMessage, Row}
import repository.hbase.HBase.DefaultColumnFamily
import repository.{LegalUnitRepository, RestRepository, RowMapper}
import repository.hbase.PeriodTableName

case class HBaseRestLegalUnitRepositoryConfig(tableName: String)

class HBaseRestLegalUnitRepository @Inject() (
    config: HBaseRestLegalUnitRepositoryConfig,
    restRepository: RestRepository,
    rowMapper: RowMapper[LegalUnit])(implicit ec: ExecutionContext) extends LegalUnitRepository with LazyLogging {

  override def retrieveLegalUnit(ern: Ern, period: Period, ubrn: Ubrn): Future[Either[ErrorMessage, Option[LegalUnit]]] = {
    logger.info(s"Retrieving Legal Unit with [$ern] [$ubrn] for [$period].")
    restRepository.findRow(tableName(period), LegalUnitQuery.byRowKey(ern, ubrn), DefaultColumnFamily).map(fromErrorOrRow)
  }

  override def findLegalUnitsForEnterprise(ern: Ern, period: Period): Future[Either[ErrorMessage, Seq[LegalUnit]]] = {
    logger.info(s"Finding Legal Units with [$ern] for [$period].")
    restRepository.findRows(tableName(period), LegalUnitQuery.forAllWith(ern), DefaultColumnFamily).map(fromErrorOrRows)
  }

  private def tableName(period: Period): String =
    PeriodTableName(config.tableName, period)

  private def fromErrorOrRow(errorOrRow: Either[ErrorMessage, Option[Row]]): Either[ErrorMessage, Option[LegalUnit]] = {
    val errorOrRows = errorOrRow.map(_.toSeq)
    fromErrorOrRows(errorOrRows).map { legalUnits =>
      require(legalUnits.size <= 1)
      legalUnits.headOption
    }
  }

  /*
   * Note that we have made the decision to fail the entire search if ANY of the rows returned from HBase cannot
   * successfully construct a valid Legal Unit.
   */
  private def fromErrorOrRows(errorOrRows: Either[ErrorMessage, Seq[Row]]): Either[ErrorMessage, Seq[LegalUnit]] = {
    logger.debug(s"Legal Unit response is [$errorOrRows].")
    errorOrRows.flatMap { rows =>
      val errorOrLegalUnits = EitherSupport.sequence(rows.map(fromRow))
      logger.debug(s"From rows to Legal Units conversion result is [$errorOrLegalUnits].")
      errorOrLegalUnits
    }
  }

  private def fromRow(row: Row): Either[ErrorMessage, LegalUnit] = {
    val optLegalUnit = rowMapper.fromRow(row)
    if (optLegalUnit.isEmpty) logger.warn(s"Unable to construct a Legal Unit from HBase [${config.tableName}] row [$row].")
    optLegalUnit.toRight("Unable to construct a Legal Unit from Row data")
  }
}
