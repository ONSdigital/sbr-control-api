package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._

/*
 * Note that routes do not support overloaded methods, so we have to give each a unique name.
 */
@Singleton
class BadRequestController @Inject() (val controllerComponents: ControllerComponents) extends BaseController {
  def badRequest(arg1: String, arg2: String): Action[AnyContent] = Action {
    BadRequest
  }

  def badRequest2(arg1: String, arg2: String, arg3: String): Action[AnyContent] = Action {
    BadRequest
  }

  def badRequest3(arg1: String, arg2: String, arg3: Option[String]): Action[AnyContent] = Action {
    BadRequest
  }
}
