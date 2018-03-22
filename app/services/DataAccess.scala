package services

import uk.gov.ons.sbr.models.DbResponse

import scala.concurrent.Future

/**
 * Created by coolit on 01/02/2018.
 */
trait DataAccess {

  def getEnterprise(id: String, period: Option[String]): Future[DbResponse]

  def getEnterpriseHistory(id: String, max: Option[Int]): Future[DbResponse]

  def getUnitLinks(id: String): Future[DbResponse]

  def getStatUnitLinks(id: String, category: String, period: String): Future[DbResponse]
}
