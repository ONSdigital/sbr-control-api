package utils

import java.io.File

import play.api.libs.json._

/**
 * Created by haqa on 05/07/2017.
 */
object Utilities {

  private def currentDirectory = new File(".").getCanonicalPath

  def errAsJson(status: Int, code: String, msg: String, cause: String = "Not traced"): JsObject = {
    Json.obj(
      "status" -> status,
      "code" -> code,
      "route_with_cause" -> cause,
      "message_en" -> msg
    )
  }

  // ret: AnyVal
  def getElement(value: Any) = {
    val res = value match {
      case None => ""
      case Some(i: Int) => i
      case Some(l: Long) => l
      case Some(z) => s""""${z}""""
      case x => s"${x.toString}"
    }
    res
  }

  def unquote(s: String) = s.replace("\"", "")

}