package services

import uk.gov.ons.sbr.models.units.{ EnterpriseUnit, UnitLinks }

import scala.concurrent.Future

/**
 * Created by coolit on 01/02/2018.
 */
trait DataAccess {

  def getEnterprise(id: String, period: Option[String]): Future[Option[EnterpriseUnit]]

  def getUnitLinks(id: String): Future[Option[List[UnitLinks]]]

  def getStatUnitLinks(id: String, category: String, period: String): Future[Option[UnitLinks]]
}
