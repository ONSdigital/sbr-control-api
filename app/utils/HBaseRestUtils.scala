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
  val CHILD_LINK = "c"
  val PARENT_LINK = "p"

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

  def jsonToMap(json: JsValue): Map[String, String] = {
    (json \ "Cell").as[Seq[JsValue]].map { cell =>
      val col = decodeBase64((cell \ "column").as[String]).split(":", columnFamilyAndValueSubstring).last
      val value = decodeBase64((cell \ "$").as[String])
      // Below is needed as the format in HBase for child vs parent links are different
      val column = col.split("_").head match {
        case (CHILD_LINK | PARENT_LINK) => col.split("_").last
        case _ => col
      }
      column -> value
    }.toMap
  }
}
