package controllers.v1

import scala.concurrent.Future

import play.api.mvc.{ Action, AnyContent, Controller }

class UnitLinksController extends Controller {

  def retrieveUnitLinks(id: String): Action[AnyContent] =
    retrieveUnitLinksWithPeriod(id, None)

  def retrieveUnitLinksWithPeriod(id: String, periodOptStr: Option[String]): Action[AnyContent] = Action.async {
    Future.successful(Ok("Hello SBR"))
  }
}
