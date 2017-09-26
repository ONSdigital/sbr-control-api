package Services


import javax.inject.Singleton

import scala.util.Try

import play.api.libs.json.Json

import uk.gov.ons.sbr.data.DevDummyApp.dbService
import uk.gov.ons.sbr.data.model.StatUnitLinks
import uk.gov.ons.sbr.models.units.{EnterpriseUnit, KnownUnitLinks, UnitLinks}

import utils.FutureResponse.{futureFromTry, futureSuccess}
import utils.Utilities.errAsJson
import utils._


/**
 * Created by haqa on 22/09/2017.
 */
@Singleton
class SQLConnect extends DBConnector {

  private val initSQL = dbService

  def getUnitLinksFromDB(id: String) = {
    matchByParams(Some(id)) match {
      case (x: IdRequest) =>
        val resp = Try(initSQL.getStatUnitLinks(x.id)).futureTryRes.flatMap {
          case (s: Seq[StatUnitLinks]) => if (s.nonEmpty) {
            tryAsResponse(Try(Json.toJson(s.map{v => UnitLinks(v)}))).future
          } else NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find Unit Links with id ${x.id}")).future
        } recover responseException
        resp
      case r => invalidSearchResponses(r)
    }
  }

  def getUnitLinksFromDB(id: String, period: String) =  {
    matchByParams(Some(id)) match {
      case (x: ReferencePeriodRequest) =>
        val resp = Try(initSQL.getStatUnitLinks(x.period, x.id)).futureTryRes.flatMap {
          case (s: Seq[StatUnitLinks]) => if (s.nonEmpty) {
            tryAsResponse(Try(Json.toJson(s.map{v => UnitLinks(v)}))).future
          } else
            NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id} and period ${x.period}")).future
        } recover responseException
        resp
      case r => invalidSearchResponses(r)
    }
  }

  def getEnterpriseFromDB(id: String) = {
    matchByParams(Some(id)) match {
      case (x: IdRequest) =>
        val resp = Try(initSQL.getEnterpriseAsStatUnit(id.toLong)).futureTryRes.flatMap {
          case Some(v) =>
            tryAsResponse(Try(Json.toJson(EnterpriseUnit(v)))).future
          case _ =>
            NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find Unit Links with id ${x.id}")).future
        } recover responseException
        resp
      case r => invalidSearchResponses(r)
    }
  }


  def getEnterpriseFromDB(id: String, period: String) = {
    matchByParams(Some(id)) match {
      case (x: ReferencePeriodRequest) =>
        val resp = Try(initSQL.getEnterpriseAsStatUnit(x.period, x.id.toLong)).futureTryRes.flatMap {
          case Some(v) =>
            tryAsResponse(Try(Json.toJson(EnterpriseUnit(v)))).future
          case _ =>
            NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id} and period ${x.period}")).future
        } recover responseException
        resp
      case r => invalidSearchResponses(r)
    }
  }


  def getStatUnitLinkFromDB(id: String, category: String) = {
    val idValidation = matchByParams(Some(id), None)
    val evalResp = idValidation match {
      case (x: IdRequest) =>
        CategoryRequest(x.id, category)
      case z => z
    }
    evalResp match {
      case (x: CategoryRequest) =>
        val resp = Try(initSQL.getStatUnitLinksByKey(x.id, x.category)).futureTryRes.flatMap {
          case Some(v) =>
            tryAsResponse(Try(Json.toJson(KnownUnitLinks(v)))).future
          case _ =>
            NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id} and category ${x.category}")).future
        } recover responseException
        resp
      case r => invalidSearchResponses(r)
    }
  }

  def getStatUnitLinkFromDB(id: String, period: String, category: String) = {
    matchByParams(Some(id), Some(period)) match {
      case (x: ReferencePeriodRequest) =>
        val resp = Try(initSQL.getStatUnitLinksByKey(x.period, x.id, category)).futureTryRes.flatMap {
          case Some(v) =>
            tryAsResponse(Try(Json.toJson(KnownUnitLinks(v)))).future
          case _ => NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find unit link with " +
            s"id ${x.id}, period ${x.period} and grouping $category")).future
        } recover responseException
        resp
      case x => invalidSearchResponses(x)
    }
  }



}
