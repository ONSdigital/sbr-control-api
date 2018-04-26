package repository

import scala.concurrent.Future

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern }

import repository.RestRepository.ErrorMessage

trait EnterpriseUnitRepository {
  def retrieveEnterpriseUnit(ern: Ern, period: Period): Future[Either[ErrorMessage, Option[Enterprise]]]
}
