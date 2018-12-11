package services

import uk.gov.ons.sbr.models.UnitKey
import uk.gov.ons.sbr.models.unitlinks.UnitType.{PayAsYouEarn, ValueAddedTax}

import scala.concurrent.Future

class CompositeAdminUnitRegisterService(vatRegisterService: UnitRegisterService, payeRegisterService: UnitRegisterService) extends UnitRegisterService {
  override def isRegisteredUnit(unitKey: UnitKey): Future[UnitRegisterResult] = {
    require(unitKey.unitType == ValueAddedTax || unitKey.unitType == PayAsYouEarn, s"Unsupported unit type [${unitKey.unitType}]")
    unitKey.unitType match {
      case ValueAddedTax => vatRegisterService.isRegisteredUnit(unitKey)
      case PayAsYouEarn => payeRegisterService.isRegisteredUnit(unitKey)
      /*
       * catch-all to prevent compiler warning that match is not exhaustive - but this should not be hit
       */
      case _ => Future.successful(UnitNotFound)
    }
  }
}
