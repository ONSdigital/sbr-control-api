package services
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsString
import repository._
import services.LegalUnitLinkPatchService._
import uk.gov.ons.sbr.models.UnitKey
import uk.gov.ons.sbr.models.patch.{ AddOperation, Patch, RemoveOperation, TestOperation }
import uk.gov.ons.sbr.models.unitlinks.UnitType.ValueAddedTax
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitType }

import scala.concurrent.Future

class LegalUnitLinkPatchService @Inject() (repository: UnitLinksRepository, vatRegisterService: UnitRegisterService) extends PatchService with LazyLogging {
  override def applyPatchTo(unitKey: UnitKey, patch: Patch): Future[PatchStatus] =
    patch match {
      case AddOperation(path, Vat) :: Nil if isChildPath(path) =>
        createVatChild(unitKey, childUnitIdFrom(path))
      case TestOperation(testPath, Vat) :: RemoveOperation(removePath) :: Nil if (testPath == removePath) && isChildPath(testPath) =>
        deleteVatChild(unitKey, childUnitIdFrom(testPath))
      case _ =>
        Future.successful(PatchRejected)
    }

  private def createVatChild(unitKey: UnitKey, vatUnitId: UnitId): Future[PatchStatus] = {
    logger.debug(s"Requested creation of VAT child [$vatUnitId] linked from unit [$unitKey]")
    vatRegisterService.isRegisteredUnit(UnitKey(vatUnitId, ChildUnitType, unitKey.period)).flatMap {
      case UnitFound =>
        repository.createChildLink(unitKey, ChildUnitType, vatUnitId).map(toCreateChildPatchStatus)
      case UnitNotFound =>
        Future.successful(PatchRejected)
      case UnitRegisterFailure(_) =>
        Future.successful(PatchFailed)
    }
  }

  private def deleteVatChild(unitKey: UnitKey, vatUnitId: UnitId): Future[PatchStatus] =
    repository.deleteChildLink(unitKey, ChildUnitType, vatUnitId).map(toDeleteChildPatchStatus)
}

private object LegalUnitLinkPatchService {
  val ChildUnitType = ValueAddedTax
  val Vat = JsString(UnitType.toAcronym(ChildUnitType))
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