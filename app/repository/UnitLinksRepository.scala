package repository
import scala.concurrent.Future

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitLinks, UnitType }

import repository.RestRepository.ErrorMessage

trait UnitLinksRepository {
  def retrieveUnitLinks(id: UnitId, unitType: UnitType, period: Period): Future[Either[ErrorMessage, Option[UnitLinks]]]
}
