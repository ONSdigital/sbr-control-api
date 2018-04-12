package repository.hbase.unit.enterprise

import scala.concurrent.Future

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern }

trait EnterpriseUnitRepository {
  def retrieveEnterpriseUnit(ern: Ern, period: Period): Future[Option[Enterprise]]
}
