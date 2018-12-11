package controllers

import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

private [controllers] abstract class AbstractSbrController(protected val controllerComponents: ControllerComponents) extends BaseController {
  implicit lazy protected val executionContext: ExecutionContext = defaultExecutionContext
}
