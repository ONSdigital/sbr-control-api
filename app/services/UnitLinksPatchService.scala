package services

import com.typesafe.scalalogging.LazyLogging
import uk.gov.ons.sbr.models.UnitKey
import uk.gov.ons.sbr.models.patch.Patch
import uk.gov.ons.sbr.models.unitlinks.UnitType.{ LegalUnit, PayAsYouEarn, ValueAddedTax }

import scala.concurrent.Future

class UnitLinksPatchService(adminDataPatchService: PatchService, legalUnitPatchService: PatchService) extends PatchService with LazyLogging {
  override def applyPatchTo(unitKey: UnitKey, patch: Patch): Future[PatchStatus] = {
    logger.debug(s"Received patch for [$unitKey]")
    unitKey.unitType match {
      case ValueAddedTax => adminDataPatchService.applyPatchTo(unitKey, patch)
      case PayAsYouEarn => adminDataPatchService.applyPatchTo(unitKey, patch)
      case LegalUnit => legalUnitPatchService.applyPatchTo(unitKey, patch)
      case _ => Future.successful(PatchRejected)
    }
  }
}
