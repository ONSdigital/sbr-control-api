package controllers.v1

import config.Properties.minKeyLength
import io.swagger.annotations._
import play.api.Logger
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._
import uk.gov.ons.sbr.data.domain.StatisticalUnit
import uk.gov.ons.sbr.models.units.{ EnterpriseUnit, UnitLinks }
import utils.FutureResponse.{ futureFromTry, futureSuccess }
import utils.Utilities.errAsJson
import utils._

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }
import scala.collection.JavaConversions._

/**
 * Created by coolit on 20/09/2017.
 */

/**
 * @todo
 *       - check no-param found err-control
 */
@Api("Search")
class EditController extends ControllerUtils {

  def editEnterprise(id: String): Action[AnyContent] = Action.async { implicit request =>
    Logger.info(s"Editing by default period for id: ${id}")
    val evalResp = matchByEditParams(Some(id), request)
    val res = evalResp match {
      case (x: EditRequest) => {
        val resp = Try(requestEnterprise.updateEnterpriseVariableValues(x.id, x.updatedBy, x.edits)) match {
          case Success(s) => Ok("").future
          case Failure(ex) => InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "edit_error", s"unable to make edit with exception [${ex.printStackTrace()}]")).future
        }
        resp
      }
      case (i: InvalidKey) => BadRequest(errAsJson(BAD_REQUEST, "invalid_key", s"invalid id ${i.id}. Check key size[$minKeyLength].")).future
      case (j: InvalidEditJson) => BadRequest(errAsJson(BAD_REQUEST, "invalid_edit_json",
        s"cannot parse json with exception ${j.exception}")).future
    }
    res
  }

  def editEnterpriseForPeriod(period: String, id: String): Action[AnyContent] = Action.async { implicit request =>
    Logger.info(s"Editing by period [${period}] for id: ${id}")
    val evalResp = matchByEditParams(Some(id), request, Some(period))
    val res = evalResp match {
      case (x: EditRequestByPeriod) => {
        val resp = Try(requestEnterprise.updateEnterpriseVariableValues(x.period, x.id, x.updatedBy, x.edits)) match {
          case Success(s) => Ok("").future
          case Failure(ex) => InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "edit_error", s"unable to make edit with exception [${ex.printStackTrace()}]")).future
        }
        resp
      }
      case (i: InvalidKey) => BadRequest(errAsJson(BAD_REQUEST, "invalid_key", s"invalid id ${i.id}. Check key size[$minKeyLength].")).future
      case (r: InvalidReferencePeriod) => BadRequest(errAsJson(BAD_REQUEST, "invalid_date", s"cannot parse date with exception ${r.exception}")).future
      case (j: InvalidEditJson) => BadRequest(errAsJson(BAD_REQUEST, "invalid_edit_json", s"cannot parse json with exception ${j.exception}")).future
    }
    res
  }
}
