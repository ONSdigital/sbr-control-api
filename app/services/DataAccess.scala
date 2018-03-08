package services

import uk.gov.ons.sbr.models.{ DbResponse, DbResult }

import scala.concurrent.Future

/**
 * Created by coolit on 01/02/2018.
 */
trait DataAccess {

  def getEnterprise(id: String, period: Option[String]): Future[DbResponse] // Future[Option[EnterpriseUnit]]

  def getUnitLinks(id: String): Future[DbResponse] // Future[Option[List[UnitLinks]]]

  def getStatUnitLinks(id: String, category: String, period: String): Future[DbResponse] // Future[Option[UnitLinks]]
}
