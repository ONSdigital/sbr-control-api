package services

import repository.RestRepository.ErrorMessage
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitType }

import scala.concurrent.Future

trait UnitRegisterService {
  def isRegisteredUnit(unitId: UnitId, unitType: UnitType, period: Period): Future[UnitRegisterResult]
}

sealed trait UnitRegisterResult
object UnitFound extends UnitRegisterResult
object UnitNotFound extends UnitRegisterResult
case class UnitRegisterFailure(errorMessage: ErrorMessage) extends UnitRegisterResult
