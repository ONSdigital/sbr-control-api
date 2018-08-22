package repository

import repository.RestRepository.ErrorMessage
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.{ LocalUnit, Lurn }

import scala.concurrent.Future

trait LocalUnitRepository {
  def retrieveLocalUnit(ern: Ern, period: Period, lurn: Lurn): Future[Either[ErrorMessage, Option[LocalUnit]]]
  def findLocalUnitsForEnterprise(ern: Ern, period: Period): Future[Either[ErrorMessage, Seq[LocalUnit]]]
}
