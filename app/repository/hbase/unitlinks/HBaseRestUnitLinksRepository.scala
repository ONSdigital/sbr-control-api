package repository.hbase.unitlinks

import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import repository.RestRepository.{ ErrorMessage, Row }
import repository._
import repository.hbase.unitlinks.HBaseRestUnitLinksRepository.ColumnFamily
import repository.hbase.{ Column, PeriodTableName }
import uk.gov.ons.sbr.models.unitlinks.{ UnitLinks, UnitLinksNoPeriod }
import uk.gov.ons.sbr.models.{ Period, UnitKey }

import scala.concurrent.Future

case class HBaseRestUnitLinksRepositoryConfig(tableName: String)

class HBaseRestUnitLinksRepository @Inject() (
    restRepository: RestRepository,
    config: HBaseRestUnitLinksRepositoryConfig,
    rowMapper: RowMapper[UnitLinksNoPeriod]
) extends UnitLinksRepository with LazyLogging {

  override def retrieveUnitLinks(unitKey: UnitKey): Future[Either[ErrorMessage, Option[UnitLinks]]] = {
    logger.info(s"Retrieving UnitLinks for [$unitKey]")
    restRepository.findRow(tableName(unitKey.period), UnitLinksRowKey(unitKey.unitId, unitKey.unitType), ColumnFamily).map {
      fromErrorOrRow(unitKey.period)
    }
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

  override def updateParentId(unitKey: UnitKey, updateDescriptor: UpdateParentDescriptor): Future[UpdateResult] = {
    val qualifiedColumn = Column(ColumnFamily, UnitLinksQualifier.toParent(updateDescriptor.parentType))
    restRepository.update(
      tableName(unitKey.period),
      UnitLinksRowKey(unitKey.unitId, unitKey.unitType),
      (qualifiedColumn, updateDescriptor.fromParentId.value),
      (qualifiedColumn, updateDescriptor.toParentId.value)
    )
  }
}

object HBaseRestUnitLinksRepository {
  val ColumnFamily = "l"
}