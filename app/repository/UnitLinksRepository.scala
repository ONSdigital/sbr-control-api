package repository
import scala.concurrent.Future

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{ UnitLinks, UnitType }

import repository.RestRepository.ErrorMessage

trait UnitLinksRepository {
  def retrieveUnitLinks(id: String, unitType: UnitType, period: Period): Future[Either[ErrorMessage, Option[UnitLinks]]]
}
