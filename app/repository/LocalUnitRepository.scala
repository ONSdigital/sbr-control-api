package repository

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.{ LocalUnit, Lurn }

import scala.concurrent.Future

trait LocalUnitRepository {
  def retrieveLocalUnit(ern: Ern, period: Period, lurn: Lurn): Future[Option[LocalUnit]]
}
