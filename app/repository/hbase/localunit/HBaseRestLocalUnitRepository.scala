package repository.hbase.localunit

import javax.inject.Inject

import scala.concurrent.Future

import com.typesafe.scalalogging.LazyLogging

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.{ LocalUnit, Lurn }

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
    restRepository.findRow(config.tableName, LocalUnitRowKey(ern, period, lurn), DefaultColumnGroup).map(fromErrorOrRow)
  }

  private def fromErrorOrRow(errorOrRow: Either[ErrorMessage, Option[Row]]): Either[ErrorMessage, Option[LocalUnit]] = {
    logger.debug(s"Local Unit response is [$errorOrRow].")
    errorOrRow.right.flatMap { optRow =>
      optRow.map(fromRow).fold[Either[ErrorMessage, Option[LocalUnit]]](Right(None)) { errorOrLocalUnit =>
        logger.debug(s"From row to Local Unit conversion result is [$errorOrLocalUnit].")
        errorOrLocalUnit.right.map(Some(_))
      }
    }
  }

  private def fromRow(row: Row): Either[ErrorMessage, LocalUnit] = {
    val optLocalUnit = rowMapper.fromRow(row)
    if (optLocalUnit.isEmpty) logger.warn(s"Unable to construct a Local Unit from HBase [${config.tableName}] row [$row].")
    optLocalUnit.toRight("Unable to construct a Local Unit from Row data")
  }
}
