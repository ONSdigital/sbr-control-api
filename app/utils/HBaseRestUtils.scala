package utils

import javax.inject.Inject
import javax.inject.Singleton

import com.google.common.io.BaseEncoding

import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.libs.ws.{ WSAuthScheme, WSClient, WSResponse }

import scala.concurrent.Future
import scala.concurrent.duration._

import config.Properties

@Singleton
class HBaseRestUtils @Inject() (ws: WSClient, val configuration: Configuration) extends Properties {

  def singleGETRequest(path: String, headers: Seq[(String, String)] = Seq.empty): Future[WSResponse] = ws.url(path.toString)
    .withHeaders(headers: _*)
    .withAuth(username, password, WSAuthScheme.BASIC)
    .withRequestTimeout(timeout milliseconds)
    .get

  def decodeBase64(str: String): String = new String(BaseEncoding.base64().decode(str), "UTF-8")

  def extractParents(key: String, map: Map[String, String]): Option[Map[String, String]] = key match {
    case a if (a == entUnit) => None
    case b if (b == leuUnit) => Some(map.filterKeys(_ == entUnit))
    case _ => Some(map filterKeys Set(leuUnit, entUnit))
  }

  def extractChildren(key: String, map: Map[String, String]): Option[Map[String, String]] = key match {
    case a if (a == entUnit) => Some(map)
    case b if (b == leuUnit) => Some(map - entUnit)
    case _ => None
  }

  def createEntRowKey(period: Option[String], id: String): String = String.join(delimiter, id, period.getOrElse("*"))

  /**
   * endpoint => rowKey
   * /v1/units/:id => id~*
   * /v1/periods/:period/types/:type/units/:id => id~type~period
   */
  def createUnitLinksRowKey(id: String, period: Option[String], unitType: Option[String]): String = (period, unitType) match {
    case (Some(p), Some(u)) => String.join(delimiter, id, u, p)
    case (None, None) => String.join(delimiter, id, "*")
  }

  def jsonToMap(json: JsValue, formKey: String => String): Map[String, String] = {
    (json \ "Cell").as[Seq[JsValue]].map { cell =>
      val col = decodeBase64((cell \ "column").as[String]).split(":", columnFamilyAndValueSubstring).last
      val value = decodeBase64((cell \ "$").as[String])
      // Use the passed in function to create the key for the Map
      val column = formKey(col)
      column -> value
    }.toMap
  }

  def formUnitKey(col: String): String = col.split("_").last

  def formEntKey(col: String): String = col
}
