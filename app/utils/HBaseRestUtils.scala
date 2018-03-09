package utils

import com.google.common.io.BaseEncoding
import play.api.libs.json.{ JsArray, JsLookupResult, JsValue }

/**
 * Created by coolit on 09/03/2018.
 */
object HBaseRestUtils {

  // These are duplicated, need to inject the config in?
  val ENT_UNIT = "ENT"
  val LEU_UNIT = "LEU"

  private val DELIMITER: String = "~"
  private val columnFamilyAndValueSubstring: Int = 2

  def decodeBase64(str: String): String = new String(BaseEncoding.base64().decode(str), "UTF-8")

  def createEntRowKey(period: Option[String], id: String): String = String.join(DELIMITER, id, period.getOrElse("*"))

  def extractParents(key: String, map: Map[String, String]): Option[Map[String, String]] = key match {
    case ENT_UNIT => None
    case LEU_UNIT => Some(map.filterKeys(_ == ENT_UNIT))
    case _ => Some(map filterKeys Set(LEU_UNIT, ENT_UNIT))
  }

  def extractChildren(key: String, map: Map[String, String]): Option[Map[String, String]] = key match {
    case ENT_UNIT => Some(map)
    case LEU_UNIT => Some(map - ENT_UNIT)
    case _ => None
  }

  /**
   * endpoint => rowKey
   * /v1/units/:id => id~*
   * /v1/periods/:period/types/:type/units/:id => id~type~period
   */
  def createUnitLinksRowKey(id: String, period: Option[String], unitType: Option[String]): String = (period, unitType) match {
    case (Some(p), Some(u)) => String.join(DELIMITER, id, u, p)
    case (None, None) => String.join(DELIMITER, id, "*")
  }

  def jsonToMap(unitType: String, js: JsLookupResult): Map[String, String] = {
    // An enterprise id is unique so we can safely always get the first JS value
    (js \ "Cell").as[Seq[JsValue]].map { cell =>
      val col = decodeBase64((cell \ "column").as[String]).split(":", columnFamilyAndValueSubstring).last
      val value = decodeBase64((cell \ "$").as[String])
      val column = unitType match {
        case ENT_UNIT => col
        case LEU_UNIT => col.split("_").last
      }
      column -> value
    }.toMap
  }

  def convertToUnitMap(result: JsValue): Map[String, String] = {
    val js = result.as[JsArray]
    val columnFamilyAndValueSubstring = 2
    (js(0) \ "Cell").as[Seq[JsValue]].map { cell =>
      val column = decodeBase64((cell \ "column").as[String])
        .split(":", columnFamilyAndValueSubstring).last
        .split("_").last
      val value = decodeBase64((cell \ "$").as[String])
      column -> value
    }.toMap
  }
}
