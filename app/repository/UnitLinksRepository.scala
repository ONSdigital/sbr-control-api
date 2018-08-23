package repository

import repository.RestRepository.ErrorMessage
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitLinks, UnitType }

import scala.concurrent.Future

trait UnitLinksRepository {
  def retrieveUnitLinks(id: UnitId, unitType: UnitType, period: Period): Future[Either[ErrorMessage, Option[UnitLinks]]]

  def updateParentId(id: UnitId, unitType: UnitType, period: Period, parentType: UnitType, fromParentId: UnitId, toParentId: UnitId): Future[UpdateResult]
}
