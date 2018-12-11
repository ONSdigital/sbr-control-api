package services

import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import play.api.libs.json.JsString
import repository._
import services.LegalUnitLinkPatchService._
import uk.gov.ons.sbr.models.UnitKey
import uk.gov.ons.sbr.models.patch.{AddOperation, Patch, RemoveOperation, TestOperation}
import uk.gov.ons.sbr.models.unitlinks.UnitType.{PayAsYouEarn, ValueAddedTax}
import uk.gov.ons.sbr.models.unitlinks.{UnitId, UnitType}

import scala.concurrent.{ExecutionContext, Future}

class LegalUnitLinkPatchService @Inject() (repository: UnitLinksRepository,
                                           unitRegisterService: UnitRegisterService)
                                          (implicit ec: ExecutionContext) extends PatchService with LazyLogging {
  override def applyPatchTo(unitKey: UnitKey, patch: Patch): Future[PatchStatus] =
    patch match {
      case AddOperation(path, Vat) :: Nil if isChildPath(path) =>
        createChild(unitKey, childUnitIdFrom(path), ValueAddedTax)
      case AddOperation(path, Paye) :: Nil if isChildPath(path) =>
        createChild(unitKey, childUnitIdFrom(path), PayAsYouEarn)
      case TestOperation(testPath, Vat) :: RemoveOperation(removePath) :: Nil if (testPath == removePath) && isChildPath(testPath) =>
        deleteChild(unitKey, childUnitIdFrom(testPath), ValueAddedTax)
      case TestOperation(testPath, Paye) :: RemoveOperation(removePath) :: Nil if (testPath == removePath) && isChildPath(testPath) =>
        deleteChild(unitKey, childUnitIdFrom(testPath), PayAsYouEarn)
      case _ =>
        Future.successful(PatchRejected)
    }

  private def createChild(unitKey: UnitKey, childUnitId: UnitId, childUnitType: UnitType): Future[PatchStatus] = {
    logger.debug(s"Requested creation of child with id=[$childUnitId] and type=[$childUnitType] linked from unit [$unitKey]")
    unitRegisterService.isRegisteredUnit(UnitKey(childUnitId, childUnitType, unitKey.period)).flatMap {
      case UnitFound =>
        repository.createChildLink(unitKey, childUnitType, childUnitId).map(toCreateChildPatchStatus)
      case UnitNotFound =>
        Future.successful(PatchRejected)
      case UnitRegisterFailure(_) =>
        Future.successful(PatchFailed)
    }
  }

  private def deleteChild(unitKey: UnitKey, childUnitId: UnitId, childUnitType: UnitType): Future[PatchStatus] = {
    logger.debug(s"Requested deletion of child with id=[$childUnitId] and type=[$childUnitType] linked from unit [$unitKey]")
    repository.deleteChildLink(unitKey, childUnitType, childUnitId).map(toDeleteChildPatchStatus)
  }
}

private object LegalUnitLinkPatchService {
  val Vat = JsString(UnitType.toAcronym(ValueAddedTax))
  val Paye = JsString(UnitType.toAcronym(PayAsYouEarn))

  private val ChildPathPrefix = "/children/"

  def isChildPath(path: String): Boolean =
    path.startsWith(ChildPathPrefix)

  def childUnitIdFrom(path: String): UnitId = {
    require(isChildPath(path))
    UnitId(path.drop(ChildPathPrefix.length))
  }

  def toCreateChildPatchStatus(createChildLinkResult: CreateChildLinkResult): PatchStatus =
    createChildLinkResult match {
      case LinkFromUnitNotFound => PatchTargetNotFound
      case CreateChildLinkSuccess => PatchApplied
      case CreateChildLinkFailure => PatchFailed
    }

  def toDeleteChildPatchStatus(editResult: OptimisticEditResult): PatchStatus =
    editResult match {
      case EditApplied => PatchApplied
      case EditConflicted => PatchConflicted
      case EditFailed => PatchFailed
      case EditTargetNotFound => PatchTargetNotFound
    }
}