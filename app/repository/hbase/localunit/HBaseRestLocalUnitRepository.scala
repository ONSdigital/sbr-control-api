package repository.hbase.localunit

import javax.inject.Inject

import scala.concurrent.Future

import com.typesafe.scalalogging.LazyLogging

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.{ LocalUnit, Lurn }
import utils.EitherSupport

import repository.RestRepository.{ ErrorMessage, Row }
import repository.hbase.HBase.DefaultColumnGroup
import repository.{ LocalUnitRepository, RestRepository, RowMapper }
import play.api.libs.concurrent.Execution.Implicits.defaultContext

case class HBaseRestLocalUnitRepositoryConfig(tableName: String)

class HBaseRestLocalUnitRepository @Inject() (
    config: HBaseRestLocalUnitRepositoryConfig,
    restRepository: RestRepository,
    rowMapper: RowMapper[LocalUnit]
) extends LocalUnitRepository with LazyLogging {

  override def retrieveLocalUnit(ern: Ern, period: Period, lurn: Lurn): Future[Either[ErrorMessage, Option[LocalUnit]]] = {
    logger.info(s"Retrieving Local Unit with [$ern] [$lurn] for [$period].")
    restRepository.findRow(config.tableName, LocalUnitQuery.byRowKey(ern, period, lurn), DefaultColumnGroup).map(fromErrorOrRow)
  }

  override def findLocalUnitsForEnterprise(ern: Ern, period: Period): Future[Either[ErrorMessage, Seq[LocalUnit]]] = {
    logger.info(s"Finding Local Units with [$ern] for [$period].")
    restRepository.findRows(config.tableName, LocalUnitQuery.forAllWith(ern, period), DefaultColumnGroup).map(fromErrorOrRows)
  }

  private def fromErrorOrRow(errorOrRow: Either[ErrorMessage, Option[Row]]): Either[ErrorMessage, Option[LocalUnit]] = {
    val errorOrRows = errorOrRow.right.map(_.toSeq)
    fromErrorOrRows(errorOrRows).right.map { localUnits =>
      require(localUnits.size <= 1)
      localUnits.headOption
    }
  }

  /*
   * Note that we have made the decision to fail the entire search if ANY of the rows returned from HBase cannot
   * successfully construct a valid Local Unit.
   */
  private def fromErrorOrRows(errorOrRows: Either[ErrorMessage, Seq[Row]]): Either[ErrorMessage, Seq[LocalUnit]] = {
    logger.debug(s"Local Unit response is [$errorOrRows].")
    errorOrRows.right.flatMap { rows =>
      val errorOrLocalUnits = EitherSupport.sequence(rows.map(fromRow))
      logger.debug(s"From rows to Local Units conversion result is [$errorOrLocalUnits].")
      errorOrLocalUnits
    }
  }

  private def fromRow(row: Row): Either[ErrorMessage, LocalUnit] = {
    val optLocalUnit = rowMapper.fromRow(row)
    if (optLocalUnit.isEmpty) logger.warn(s"Unable to construct a Local Unit from HBase [${config.tableName}] row [$row].")
    optLocalUnit.toRight("Unable to construct a Local Unit from Row data")
  }
}
