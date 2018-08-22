package repository.hbase.unitlinks

import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import repository.RestRepository.{ ErrorMessage, Row }
import repository.hbase.PeriodTableName
import repository.hbase.unitlinks.HBaseRestUnitLinksRepository.ColumnFamily
import repository.{ RestRepository, RowMapper, UnitLinksRepository }
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitLinks, UnitLinksNoPeriod, UnitType }

import scala.concurrent.Future

case class HBaseRestUnitLinksRepositoryConfig(tableName: String)

class HBaseRestUnitLinksRepository @Inject() (
    restRepository: RestRepository,
    config: HBaseRestUnitLinksRepositoryConfig,
    rowMapper: RowMapper[UnitLinksNoPeriod]
) extends UnitLinksRepository with LazyLogging {

  override def retrieveUnitLinks(id: UnitId, unitType: UnitType, period: Period): Future[Either[ErrorMessage, Option[UnitLinks]]] = {
    logger.info(s"Retrieving UnitLinks with [$id] of [$unitType] for [$period]")
    restRepository.findRow(tableName(period), UnitLinksRowKey(id, unitType), ColumnFamily).map(fromErrorOrRow(period))
  }

  private def tableName(period: Period): String =
    PeriodTableName(config.tableName, period)

  private def fromErrorOrRow(withPeriod: Period)(errorOrRow: Either[ErrorMessage, Option[Row]]): Either[ErrorMessage, Option[UnitLinks]] = {
    logger.debug(s"Unit Links response is [$errorOrRow]")
    errorOrRow.right.flatMap { optRow =>
      optRow.map(fromRow).fold[Either[ErrorMessage, Option[UnitLinks]]](Right(None)) { errorOrUnitLinks =>
        logger.debug(s"From Row to Unit Links conversion is [$errorOrUnitLinks]")
        errorOrUnitLinks.right.map { unitLinksNoPeriod =>
          Some(UnitLinks.from(withPeriod, unitLinksNoPeriod))
        }
      }
    }
  }

  private def fromRow(row: Row): Either[ErrorMessage, UnitLinksNoPeriod] = {
    val optUnitLinks = rowMapper.fromRow(row)
    if (optUnitLinks.isEmpty) logger.warn(s"Unable to construct Unit Links from HBase [${config.tableName}] of [$row]")
    optUnitLinks.toRight("Unable to create Unit Links from row")
  }
}

object HBaseRestUnitLinksRepository {
  val ColumnFamily = "l"
}