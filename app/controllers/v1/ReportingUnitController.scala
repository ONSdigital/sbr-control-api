package controllers.v1

import javax.inject.Inject

import controllers.v1.api.ReportingUnitApi
import play.api.mvc.{ Action, AnyContent, Controller }
//import repository.ReportingUnitRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ReportingUnitController @Inject() () extends Controller with ReportingUnitApi {
  override def retrieveReportingUnit(ernStr: String, periodStr: String, lurnStr: String): Action[AnyContent] = Action.async {
    Future(Ok)
  }

  override def retrieveAllReportingUnitsForEnterprise(ernStr: String, periodStr: String): Action[AnyContent] = Action.async {
    Future(Ok)
  }

  def badRequest(ernStr: String, periodStr: String, rurnStrOpt: Option[String]) = Action {
    BadRequest
  }
}
