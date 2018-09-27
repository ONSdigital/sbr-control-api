package services
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsString
import repository._
import services.LegalUnitLinkPatchService.{ ChildPathPrefix, ChildUnitType, Vat, toPatchStatus }
import uk.gov.ons.sbr.models.UnitKey
import uk.gov.ons.sbr.models.patch.OperationTypes.Add
import uk.gov.ons.sbr.models.patch.{ Operation, Patch }
import uk.gov.ons.sbr.models.unitlinks.UnitType.ValueAddedTax
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitType }

import scala.concurrent.Future

class LegalUnitLinkPatchService @Inject() (repository: UnitLinksRepository, vatRegisterService: UnitRegisterService) extends PatchService with LazyLogging {
  override def applyPatchTo(unitKey: UnitKey, patch: Patch): Future[PatchStatus] =
    patch match {
      case Operation(Add, path, Vat) :: Nil if path.startsWith(ChildPathPrefix) =>
        val vatRef = path.drop(ChildPathPrefix.length)
        createVatChild(unitKey, UnitId(vatRef))
      case _ =>
        Future.successful(PatchRejected)
    }

  private def createVatChild(unitKey: UnitKey, vatUnitId: UnitId): Future[PatchStatus] = {
    logger.debug(s"Requested creation of VAT child [$vatUnitId] linked from unit [$unitKey]")
    vatRegisterService.isRegisteredUnit(UnitKey(vatUnitId, ChildUnitType, unitKey.period)).flatMap {
      case UnitFound =>
        repository.createChildLink(unitKey, ChildUnitType, vatUnitId).map(toPatchStatus)
      case UnitNotFound =>
        Future.successful(PatchRejected)
      case UnitRegisterFailure(_) =>
        Future.successful(PatchFailed)
    }
  }
}

private object LegalUnitLinkPatchService {
  val ChildPathPrefix = "/children/"
  val ChildUnitType = ValueAddedTax
  val Vat = JsString(UnitType.toAcronym(ChildUnitType))

  def toPatchStatus(createChildLinkResult: CreateChildLinkResult): PatchStatus =
    createChildLinkResult match {
      case LinkFromUnitNotFound => PatchTargetNotFound
      case CreateChildLinkSuccess => PatchApplied
      case CreateChildLinkFailure => PatchFailed
    }
}