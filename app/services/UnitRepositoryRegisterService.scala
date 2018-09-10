package services

import javax.inject.Inject
import repository.UnitLinksRepository
import uk.gov.ons.sbr.models.UnitKey

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/*
 * This service abstracts over how we determine whether or not a unit exists in the register.
 *
 * In this implementation, we simply check that it exists in the unit links table.
 * It would be preferable to guarantee consistency and also check the relevant unit table.
 * In the name of expediency, we are currently relying on the existing GET methods.  As we do not inspect
 * the response body, it would be preferable to use a HEAD request for such checks.
 */
class UnitRepositoryRegisterService @Inject() (unitLinksRepository: UnitLinksRepository) extends UnitRegisterService {
  override def isRegisteredUnit(unitKey: UnitKey): Future[UnitRegisterResult] =
    unitLinksRepository.retrieveUnitLinks(unitKey).map {
      _.fold[UnitRegisterResult](
        errorMessage => UnitRegisterFailure(errorMessage),
        optUnitLinks => optUnitLinks.fold[UnitRegisterResult](UnitNotFound)(_ => UnitFound)
      )
    }
}
