package services

import uk.gov.ons.sbr.models.DbResult

import scala.concurrent.Future

/**
 * Created by coolit on 01/02/2018.
 */
trait DataAccess {

  def getEnterprise(id: String, period: Option[String]): Future[DbResult] // Future[Option[EnterpriseUnit]]

  def getUnitLinks(id: String): Future[DbResult] // Future[Option[List[UnitLinks]]]

  def getStatUnitLinks(id: String, category: String, period: String): Future[DbResult] // Future[Option[UnitLinks]]
}
