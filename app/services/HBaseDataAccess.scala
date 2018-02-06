package services

import java.time.YearMonth
import java.util.Optional
import javax.inject.Inject

import play.api.mvc.{ AnyContent, Request, Result }

import uk.gov.ons.sbr.data.controller.{ EnterpriseController, UnitController }
import uk.gov.ons.sbr.data.domain.{ Enterprise, StatisticalUnit, StatisticalUnitLinks, UnitType }

import utils.Utilities._
import utils.{ CategoryRequest, IdRequest, ReferencePeriod, RequestEvaluation }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

import play.api.Configuration

import utils.FutureResponse.{ futureFromTry, futureSuccess }

class HBaseDataAccess @Inject() (implicit val configuration: Configuration) extends DataAccess {

  HBaseInMemoryConfig
  private val requestLinks = new UnitController()
  private val requestEnterprise = new EnterpriseController()

  def getUnitLinksFromDB(id: String)(implicit request: Request[AnyContent]) = {
    val evalResp = matchByParams(Some(id))
    search[java.util.List[StatisticalUnit]](evalResp, requestLinks.findUnits)
  }

  def getUnitLinksFromDB(id: String, period: String)(implicit request: Request[AnyContent]) = {
    val evalResp = matchByParams(Some(id), Some(period))
    searchByPeriod[java.util.List[StatisticalUnit]](evalResp, requestLinks.findUnits)
  }

  def getEnterpriseFromDB(id: String)(implicit request: Request[AnyContent]) = {
    val evalResp = matchByParams(Some(id))
    search[Enterprise](evalResp, requestEnterprise.getEnterprise)
  }

  def getEnterpriseFromDB(id: String, period: String)(implicit request: Request[AnyContent]) = {
    val evalResp = matchByParams(Some(id), Some(period))
    searchByPeriod[Enterprise](evalResp, requestEnterprise.getEnterpriseForReferencePeriod)
  }

  def getStatUnitLinkFromDB(id: String, category: String)(implicit request: Request[AnyContent]) = {
    val evalResp = matchByParams(Some(id), None) match {
      case (x: IdRequest) =>
        CategoryRequest(x.id, category)
      case z => z
    }
    evalResp match {
      case (x: CategoryRequest) =>
        val resp = Try(requestLinks.getUnitLinks(x.id, UnitType.fromString(x.category))).futureTryRes.flatMap {
          case (s: Optional[StatisticalUnitLinks]) => if (s.isPresent) {
            resultMatcher[StatisticalUnitLinks](s)
          } else NotFound(errAsJson(NOT_FOUND, "not_found",
            s"Could not find unit with id ${x.id} and category ${x.category}")).future
        } recover responseException
        resp
      case _ => invalidSearchResponses(evalResp)
    }
  }

  def getStatUnitLinkFromDB(id: String, period: String, category: String)(implicit request: Request[AnyContent]) = {
    matchByParams(Some(id), Some(period)) match {
      case (x: ReferencePeriod) =>
        val resp = Try(requestLinks.getUnitLinks(x.period, x.id, UnitType.fromString(category))).futureTryRes.flatMap {
          case (s: Optional[StatisticalUnitLinks]) => if (s.isPresent) {
            resultMatcher[StatisticalUnitLinks](s)
          } else NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find unit link with " +
            s"id ${x.id}, period ${x.period} and category $category")).future
        } recover responseException
        resp
      case x => invalidSearchResponses(x)
    }
  }

  private def search[X](eval: RequestEvaluation, funcWithId: String => Optional[X]): Future[Result] = {
    val res = eval match {
      case (x: IdRequest) =>
        val resp = Try(funcWithId(x.id)).futureTryRes.flatMap {
          case (s: Optional[X]) => if (s.isPresent) {
            resultMatcher[X](s)
          } else NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id}")).future
        } recover responseException
        resp
      case _ => invalidSearchResponses(eval)
    }
    res
  }

  //  @todo - combine ReferencePeriod and UnitGrouping
  private def searchByPeriod[X](eval: RequestEvaluation, funcWithIdAndParam: (YearMonth, String) => Optional[X]): Future[Result] = {
    val res = eval match {
      case (x: ReferencePeriod) =>
        val resp = Try(funcWithIdAndParam(x.period, x.id)).futureTryRes.flatMap {
          case (s: Optional[X]) => if (s.isPresent) {
            resultMatcher[X](s)
          } else NotFound(errAsJson(NOT_FOUND, "not_found",
            s"Could not find enterprise with id ${x.id} and period ${x.period}")).future
        } recover responseException
        resp
      case _ => invalidSearchResponses(eval)
    }
    res
  }
}
