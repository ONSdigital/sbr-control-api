package repository

import repository.RestRepository.ErrorMessage
import uk.gov.ons.sbr.models.UnitKey
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitLinks, UnitType }

import scala.concurrent.Future

case class UpdateParentDescriptor(parentType: UnitType, fromParentId: UnitId, toParentId: UnitId)

sealed trait CreateChildLinkResult
object LinkFromUnitNotFound extends CreateChildLinkResult
object CreateChildLinkSuccess extends CreateChildLinkResult
object CreateChildLinkFailure extends CreateChildLinkResult

trait UnitLinksRepository {
  def retrieveUnitLinks(unitKey: UnitKey): Future[Either[ErrorMessage, Option[UnitLinks]]]

  def updateParentLink(unitKey: UnitKey, updateDescriptor: UpdateParentDescriptor): Future[OptimisticEditResult]
  def createChildLink(unitKey: UnitKey, childType: UnitType, childId: UnitId): Future[CreateChildLinkResult]
  def deleteChildLink(unitKey: UnitKey, childType: UnitType, childId: UnitId): Future[OptimisticEditResult]
}
