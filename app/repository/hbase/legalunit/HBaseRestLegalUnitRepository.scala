package repository.hbase.legalunit

import javax.inject.Inject

import scala.concurrent.Future

import com.typesafe.scalalogging.LazyLogging

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.{ LegalUnit, UBRN }
import utils.EitherSupport

import repository.RestRepository.{ ErrorMessage, Row }
import repository.hbase.HBase.DefaultColumnFamily
import repository.{ LegalUnitRepository, RestRepository, RowMapper }
import play.api.libs.concurrent.Execution.Implicits.defaultContext

case class HBaseRestLegalUnitRepositoryConfig(tableName: String)

class HBaseRestLegalUnitRepository @Inject() (
    config: HBaseRestLegalUnitRepositoryConfig,
    restRepository: RestRepository,
    rowMapper: RowMapper[LegalUnit]
) extends LegalUnitRepository with LazyLogging {

  override def retrieveLegalUnit(ern: Ern, period: Period, ubrn: UBRN): Future[Either[ErrorMessage, Option[LegalUnit]]] = {
    logger.info(s"Retrieving Legal Unit with [$ern] [$ubrn] for [$period].")
    restRepository.findRow(config.tableName, LegalUnitQuery.byRowKey(ern, period, ubrn), DefaultColumnFamily).map(fromErrorOrRow)
  }

  override def findLegalUnitsForEnterprise(ern: Ern, period: Period): Future[Either[ErrorMessage, Seq[LegalUnit]]] = {
    logger.info(s"Finding Legal Units with [$ern] for [$period].")
    restRepository.findRows(config.tableName, LegalUnitQuery.forAllWith(ern, period), DefaultColumnFamily).map(fromErrorOrRows)
  }

  private def fromErrorOrRow(errorOrRow: Either[ErrorMessage, Option[Row]]): Either[ErrorMessage, Option[LegalUnit]] = {
    val errorOrRows = errorOrRow.right.map(_.toSeq)
    fromErrorOrRows(errorOrRows).right.map { legalUnits =>
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
    errorOrRows.right.flatMap { rows =>
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
