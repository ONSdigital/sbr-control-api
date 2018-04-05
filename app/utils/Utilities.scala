package utils

import play.api.libs.json._

object Utilities {

  def errAsJson(status: Int, code: String, msg: String, cause: String = "Not traced"): JsObject = {
    Json.obj(
      "status" -> status,
      "code" -> code,
      "route_with_cause" -> cause,
      "message_en" -> msg
    )
  }

  def unquote(s: String) = s.replace("\"", "")
}