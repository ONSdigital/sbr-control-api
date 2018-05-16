package repository.hbase.unitlinks

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitLinks, UnitType }

import repository.RestRepository.{ ErrorMessage, Row }
import repository.hbase.HBase.LinksColumnFamily
import repository.{ RestRepository, RowMapper, UnitLinksRepository }

case class HBaseRestUnitLinksRepositoryConfig(tableName: String)

class HBaseRestUnitLinksRepository @Inject() (
    restRepository: RestRepository,
    config: HBaseRestUnitLinksRepositoryConfig,
    rowMapper: RowMapper[UnitLinks]
) extends UnitLinksRepository with LazyLogging {

  override def retrieveUnitLinks(id: UnitId, unitType: UnitType, period: Period): Future[Either[ErrorMessage, Option[UnitLinks]]] = {
    logger.info(s"Retrieving UnitLinks with [$id] of [$unitType] for [$period]")
    restRepository.findRow(config.tableName, UnitLinksRowKey(id, unitType, period), LinksColumnFamily).map(fromErrorOrRow)
  }

  private def fromErrorOrRow(errorOrRow: Either[ErrorMessage, Option[Row]]): Either[ErrorMessage, Option[UnitLinks]] = {
    logger.debug(s"Unit Links response is [$errorOrRow]")
    errorOrRow.right.flatMap { optRow =>
      optRow.map(fromRow).fold[Either[ErrorMessage, Option[UnitLinks]]](Right(None)) { errorOrUnitLinks =>
        logger.debug(s"From Row to Unit Links conversion is [$errorOrUnitLinks]")
        errorOrUnitLinks.right.map(Some(_))
      }
    }
  }

  private def fromRow(row: Row): Either[ErrorMessage, UnitLinks] = {
    val optUnitLinks = rowMapper.fromRow(row)
    if (optUnitLinks.isEmpty) logger.warn(s"Unable to construct Unit Links from HBase [${config.tableName}] of [$row]")
    optUnitLinks.toRight("Unable to create Unit Links from row")
  }

}
