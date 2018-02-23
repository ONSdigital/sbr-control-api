package utils

import java.io.File

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import play.api.libs.json._

/**
 * Created by haqa on 05/07/2017.
 */
object Utilities {

  private val DELIMITER: String = "~"

  private def currentDirectory = new File(".").getCanonicalPath

  def encodeBase64(str: Seq[String], deliminator: String = ":"): String =
    BaseEncoding.base64.encode(str.mkString(deliminator).getBytes(Charsets.UTF_8))

  def decodeBase64(str: String): String = new String(BaseEncoding.base64().decode(str), "UTF-8")

  def errAsJson(status: Int, code: String, msg: String, cause: String = "Not traced"): JsObject = {
    Json.obj(
      "status" -> status,
      "code" -> code,
      "route_with_cause" -> cause,
      "message_en" -> msg
    )
  }

  def createEntRowKey(period: String, id: String): String = String.join(DELIMITER, id, period)

  def createUnitLinksRowKey(period: String, id: String, unitType: Option[String]): String = unitType match {
    case Some(u) => String.join(DELIMITER, period, id, u)
    case None => String.join(DELIMITER, period, id, "*")
  }

  def createTableNameWithNameSpace(nameSpace: String, tableName: String): String = s"$nameSpace:$tableName"

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