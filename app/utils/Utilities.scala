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

  def createEntRowKey(period: Option[String], id: String): String = String.join(DELIMITER, id, period.getOrElse("*"))

  /**
   * endpoint => rowKey
   * /v1/units/:id => id~*
   * /v1/periods/:period/types/:type/units/:id => id~type~period
   */
  def createUnitLinksRowKey(id: String, period: Option[String], unitType: Option[String]): String = (period, unitType) match {
    case (Some(p), Some(u)) => String.join(DELIMITER, id, u, p)
    case (None, None) => String.join(DELIMITER, id, "*")
  }

  def createTableNameWithNameSpace(nameSpace: String, tableName: String): String = s"$nameSpace:$tableName"

  def unquote(s: String) = s.replace("\"", "")
}