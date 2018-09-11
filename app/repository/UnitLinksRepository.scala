package repository

import repository.RestRepository.ErrorMessage
import uk.gov.ons.sbr.models.UnitKey
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitLinks, UnitType }

import scala.concurrent.Future

case class UpdateParentDescriptor(parentType: UnitType, fromParentId: UnitId, toParentId: UnitId)

trait UnitLinksRepository {
  def retrieveUnitLinks(unitKey: UnitKey): Future[Either[ErrorMessage, Option[UnitLinks]]]
  def updateParentId(unitKey: UnitKey, updateDescriptor: UpdateParentDescriptor): Future[UpdateResult]
}
