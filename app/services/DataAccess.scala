package services

import uk.gov.ons.sbr.models.units.{ EnterpriseUnit, UnitLinks }

import scala.concurrent.Future

/**
 * Created by coolit on 01/02/2018.
 */
trait DataAccess {

  def getUnitLinks(id: String, period: String): Future[Option[List[UnitLinks]]]

  def getEnterprise(id: String, period: Option[String]): Future[Option[EnterpriseUnit]]

  def getStatUnitLinks(id: String, category: String, period: String): Future[Option[UnitLinks]]
}
