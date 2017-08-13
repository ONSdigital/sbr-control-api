package utils

import java.io.File

import play.api.libs.json._
import uk.gov.ons.sbr.data.domain.Enterprise

/**
 * Created by haqa on 05/07/2017.
 */
object Utilities {

  def currentDirectory = new File(".").getCanonicalPath

  def errAsJson(status: Int, code: String, msg: String): JsObject = {
    Json.obj(
      "status" -> status,
      "code" -> code,
      "message_en" -> msg
    )
  }

  // ret: AnyVal
  def getElement(value: Any) = {
    val res = value match {
      case None => ""
//      case JsDefined(v) => v
      case Some(i: Int) => i
      case Some(l: Long) => l
      case Some(z) => s""""${z}""""
      case x => s"${x.toString}"
    }
    res
  }

  def unquote(s: String) = s.replace("\"", "")

  def instanceName(s: String, regex: String = "."): String = s.substring(s.lastIndexOf(regex) + 1)

}