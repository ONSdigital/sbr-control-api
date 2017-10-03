package controllers.v1

import play.api.mvc.{ AnyContent, Controller, Request }
import play.mvc.Security

/**
 * Created by haqa on 29/09/2017.
 */
class SecureController extends Controller {

  @Security.Authenticated
  def authenticationInit()(implicit request: Request[AnyContent]) = {
    request.host match {
      case r if r == "test" | r == "prod" => ???

      case _ => ???
    }

  }

}
