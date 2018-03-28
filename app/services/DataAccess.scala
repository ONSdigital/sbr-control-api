package services

import uk.gov.ons.sbr.models.DbResponse

import scala.concurrent.Future

trait DataAccess {

  def getEnterprise(id: String, period: Option[String]): Future[DbResponse]

  def getUnitLinks(id: String): Future[DbResponse]

  def getStatUnitLinks(id: String, category: String, period: String): Future[DbResponse]
}
