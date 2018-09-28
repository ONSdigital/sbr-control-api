package repository.hbase.unitlinks

import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import repository.RestRepository.{ ErrorMessage, Row }
import repository._
import repository.hbase.unitlinks.HBaseRestUnitLinksRepository._
import repository.hbase.{ Column, PeriodTableName }
import services.{ UnitFound, UnitNotFound, UnitRegisterFailure, UnitRegisterService }
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitLinks, UnitLinksNoPeriod, UnitType }
import uk.gov.ons.sbr.models.{ Period, UnitKey }

import scala.concurrent.Future

case class HBaseRestUnitLinksRepositoryConfig(tableName: String)

class HBaseRestUnitLinksRepository @Inject() (
    restRepository: RestRepository,
    config: HBaseRestUnitLinksRepositoryConfig,
    rowMapper: RowMapper[UnitLinksNoPeriod],
    unitRegisterService: UnitRegisterService
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

  /*
   * We only allow a new column to be created on an existing rowKey, and so must explicitly check first.
   */
  override def createChildLink(unitKey: UnitKey, childType: UnitType, childId: UnitId): Future[CreateChildLinkResult] =
    unitRegisterService.isRegisteredUnit(unitKey).flatMap {
      case UnitNotFound => Future.successful(LinkFromUnitNotFound)
      case UnitFound => doCreateChildLink(unitKey, childType, childId)
      case UnitRegisterFailure(_) => Future.successful(CreateChildLinkFailure)
    }

  private def doCreateChildLink(unitKey: UnitKey, childType: UnitType, childId: UnitId): Future[CreateChildLinkResult] =
    restRepository.createOrReplace(
      tableName(unitKey.period),
      UnitLinksRowKey(unitKey.unitId, unitKey.unitType),
      field = (columnNameFor(UnitLinksQualifier.toChild(childId)), UnitType.toAcronym(childType))
    ).map(toCreateChildLinkResult)

  override def updateParentLink(unitKey: UnitKey, updateDescriptor: UpdateParentDescriptor): Future[OptimisticEditResult] = {
    val columnName = columnNameFor(UnitLinksQualifier.toParent(updateDescriptor.parentType))
    restRepository.updateField(
      tableName(unitKey.period),
      UnitLinksRowKey(unitKey.unitId, unitKey.unitType),
      checkField = (columnName, updateDescriptor.fromParentId.value),
      updateField = (columnName, updateDescriptor.toParentId.value)
    )
  }

  override def deleteChildLink(unitKey: UnitKey, childType: UnitType, childId: UnitId): Future[OptimisticEditResult] = {
    val columnName = columnNameFor(UnitLinksQualifier.toChild(childId))
    restRepository.deleteField(
      tableName(unitKey.period),
      UnitLinksRowKey(unitKey.unitId, unitKey.unitType),
      checkField = columnName -> UnitType.toAcronym(childType),
      columnName
    )
  }
}

object HBaseRestUnitLinksRepository {
  val ColumnFamily = "l"

  def columnNameFor(qualifier: String): Column =
    Column(ColumnFamily, qualifier)

  private def toCreateChildLinkResult(createResult: CreateOrReplaceResult): CreateChildLinkResult =
    createResult match {
      case EditApplied => CreateChildLinkSuccess
      case EditFailed => CreateChildLinkFailure
    }
}