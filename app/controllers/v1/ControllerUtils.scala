package controllers.v1

import play.api.libs.json.Json.toJson
import play.api.libs.json.Writes
import play.api.mvc.{ Result, Results }

object ControllerUtils extends Results {

  def resultOnFailure(errorMessage: String): Result =
    errorMessage match {
      case _ if errorMessage.startsWith("Timeout") => GatewayTimeout
      case _ => InternalServerError
    }

  def resultOnSuccess[T](optUnit: Option[T])(implicit writes: Writes[T]): Result =
    optUnit.fold[Result](NotFound)(unit => Ok(toJson(unit)))

}
