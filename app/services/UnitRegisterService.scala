package services

import repository.RestRepository.ErrorMessage
import uk.gov.ons.sbr.models.UnitKey

import scala.concurrent.Future

trait UnitRegisterService {
  def isRegisteredUnit(unitKey: UnitKey): Future[UnitRegisterResult]
}

sealed trait UnitRegisterResult
object UnitFound extends UnitRegisterResult
object UnitNotFound extends UnitRegisterResult
case class UnitRegisterFailure(errorMessage: ErrorMessage) extends UnitRegisterResult
