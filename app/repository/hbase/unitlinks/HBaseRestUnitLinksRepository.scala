package repository.hbase.unitlinks

import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import repository.RestRepository.{ErrorMessage, Field, Row, RowKey}
import repository._
import repository.hbase.unitlinks.HBaseRestUnitLinksRepository._
import repository.hbase.{Column, PeriodTableName}
import services.{UnitFound, UnitNotFound, UnitRegisterFailure, UnitRegisterService}
import uk.gov.ons.sbr.models.unitlinks.{UnitId, UnitLinks, UnitLinksNoPeriod, UnitType}
import uk.gov.ons.sbr.models.{Period, UnitKey}

import scala.Function.uncurried
import scala.concurrent.{ExecutionContext, Future}

case class HBaseRestUnitLinksRepositoryConfig(tableName: String)

class HBaseRestUnitLinksRepository @Inject() (
    restRepository: RestRepository,
    config: HBaseRestUnitLinksRepositoryConfig,
    rowMapper: RowMapper[UnitLinksNoPeriod],
    unitRegisterService: UnitRegisterService)(implicit ec: ExecutionContext) extends UnitLinksRepository with LazyLogging {

  override def retrieveUnitLinks(unitKey: UnitKey): Future[Either[ErrorMessage, Option[UnitLinks]]] = {
    logger.info(s"Retrieving UnitLinks for [$unitKey]")
    restRepository.findRow(tableName(unitKey.period), UnitLinksRowKey(unitKey.unitId, unitKey.unitType), ColumnFamily).map {
      fromErrorOrRow(unitKey.period)
    }
  }

  private def fromErrorOrRow(withPeriod: Period)(errorOrRow: Either[ErrorMessage, Option[Row]]): Either[ErrorMessage, Option[UnitLinks]] = {
    logger.debug(s"Unit Links response is [$errorOrRow]")
    errorOrRow.flatMap { optRow =>
      optRow.map(fromRow).fold[Either[ErrorMessage, Option[UnitLinks]]](Right(None)) { errorOrUnitLinks =>
        logger.debug(s"From Row to Unit Links conversion is [$errorOrUnitLinks]")
        errorOrUnitLinks.map { unitLinksNoPeriod =>
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
    createOrReplace(
      unitKey,
      field = (columnNameFor(UnitLinksQualifier.toChild(childId)), UnitType.toAcronym(childType)),
      otherFields = EditedFlag
    ).map(toCreateChildLinkResult)

  private def createOrReplace(unitKey: UnitKey, field: Field, otherFields: Field*): Future[CreateOrReplaceResult] = {
    val createOnRow = uncurried(rowCommand((restRepository.createOrReplace _).curried, unitKey))
    createOnRow(field, otherFields)
  }

  override def updateParentLink(unitKey: UnitKey, updateDescriptor: UpdateParentDescriptor): Future[OptimisticEditResult] = {
    val columnName = columnNameFor(UnitLinksQualifier.toParent(updateDescriptor.parentType))
    val updateRow = uncurried(rowCommand((restRepository.updateField _).curried, unitKey))
    updateRow(
      columnName -> updateDescriptor.fromParentId.value, // checkField
      columnName -> updateDescriptor.toParentId.value, // updateField
      Seq(EditedFlag) // otherUpdateFields
    )
  }

  override def deleteChildLink(unitKey: UnitKey, childType: UnitType, childId: UnitId): Future[OptimisticEditResult] = {
    val columnName = columnNameFor(UnitLinksQualifier.toChild(childId))
    val deleteFromRow = uncurried(rowCommand((restRepository.deleteField _).curried, unitKey))
    deleteFromRow(
      columnName -> UnitType.toAcronym(childType),
      columnName
    ).flatMap {
        case EditApplied => setEditedFlag(unitKey)
        case editResult => Future.successful(editResult)
      }
  }

  private def setEditedFlag(unitKey: UnitKey): Future[OptimisticEditResult] =
    createOrReplace(unitKey, EditedFlag).map(toOptimisticEditResult)

  /*
   * Partially applies the supplied repository function.
   * The tableName & rowKey are applied as the first and second arguments.
   * In order to cater for repository functions of different arity, f must be supplied in curried form.  This implies
   * that the return type A is itself a curried function.
   */
  private def rowCommand[A](f: String => RowKey => A, unitKey: UnitKey): A =
    f(tableName(unitKey.period))(UnitLinksRowKey(unitKey.unitId, unitKey.unitType))

  private def tableName(period: Period): String =
    PeriodTableName(config.tableName, period)
}

object HBaseRestUnitLinksRepository {
  val ColumnFamily = "l"
  private val EditedFlag = columnNameFor("edited") -> "Y"

  def columnNameFor(qualifier: String): Column =
    Column(ColumnFamily, qualifier)

  private def toCreateChildLinkResult(createResult: CreateOrReplaceResult): CreateChildLinkResult =
    createResult match {
      case EditApplied => CreateChildLinkSuccess
      case EditFailed => CreateChildLinkFailure
    }

  private def toOptimisticEditResult(createResult: CreateOrReplaceResult): OptimisticEditResult =
    createResult match {
      case EditApplied => EditApplied
      case EditFailed => EditFailed
    }
}