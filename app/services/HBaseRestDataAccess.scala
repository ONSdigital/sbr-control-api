package services

import javax.inject.Inject

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import com.netaporter.uri.dsl._
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.ws.{ WSClient, WSResponse }
import play.api.http.Status
import play.api.Configuration
import play.api.libs.json.{ JsArray, JsValue }
import uk.gov.ons.sbr.data.domain.UnitType
import uk.gov.ons.sbr.models.units.UnitLinks
import uk.gov.ons.sbr.models.units.EnterpriseUnit
import utils._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by coolit on 05/02/2018.
 */
class HBaseRestDataAccess @Inject() (ws: WSClient, val configuration: Configuration) extends DataAccess with HBaseConfig with LazyLogging {

  private val columnFamilyAndValueSubstring: Int = 2
  private val DELIMITER: String = "~"
  private val AUTH = encodeBase64(Seq(username, password))
  private val HEADERS = Seq("Accept" -> "application/json", "Authorization" -> s"Basic $AUTH")

  def getUnitLinks(id: String, period: String): Future[Option[List[UnitLinks]]] =
    getStatAndUnitLinks[List[UnitLinks]](id, period, None, transformUnitSeqJson)

  def getEnterprise(id: String, period: String): Future[Option[EnterpriseUnit]] = {
    // HBase key format: 201706~9901566115, period~id
    val rowKey = createRowKey(period, id)
    val uri = baseUrl / enterpriseTableName.getNameWithNamespaceInclAsString / rowKey / columnFamily
    val r = singleGETRequest(uri.toString, HEADERS) map {
      case response if response.status == Status.OK => {
        val row = (response.json \ "Row").as[JsValue]
        Some(EnterpriseUnit(id.toLong, period, jsToEntMap(row), "ENT", List()))
      }
      case response if response.status == Status.NOT_FOUND => None
      case _ => None
    }
    logger.error(s"ent: ${r}")
    r
  }

  def getStatUnitLinks(id: String, category: String, period: String): Future[Option[UnitLinks]] =
    getStatAndUnitLinks[UnitLinks](id, period, Some(category), transformStatSeqJson)

  def getStatAndUnitLinks[T](id: String, period: String, unitType: Option[String], f: (String, Seq[JsValue], JsValue) => T): Future[Option[T]] = {
    // HBase key format: 201706~01752564~CH, period~id~type
    // When there is no unitType, * is used to get rows of any unit type
    val rowKey = createRowKey(period, id, None)
    val uri = baseUrl / unitTableName.getNameWithNamespaceInclAsString / rowKey / columnFamily
    val r = singleGETRequest(uri.toString, HEADERS) map {
      case response if response.status == Status.OK => {
        val row = (response.json \ "Row").as[JsValue]
        val seqJSON = row.as[Seq[JsValue]]
        // Depending on whether the unitType is present or not, we use a different function to transform the JSON
        // from HBase, either returning UnitLinks (when unitType is present) or List[UnitLinks] when there is
        // no unitType present
        unitType match {
          case Some(_) => Some(f(id, seqJSON, row))
          case None => Some(f(id, seqJSON, row))
        }
      }
      case response if response.status == Status.NOT_FOUND => None
      case _ => None
    }
    logger.error(s"stat/unit: ${r}")
    r
  }

  def transformStatSeqJson(id: String, seqJSON: Seq[JsValue], row: JsValue): UnitLinks = {
    val unit = decodeBase64((seqJSON(0) \ "key").as[String]).split("~").last
    UnitLinks(id, extractParents(unit, convertToUnitMap(row)), extractChildren(unit, convertToUnitMap(row)), unit)
  }

  def transformUnitSeqJson(id: String, seqJSON: Seq[JsValue], row: JsValue): List[UnitLinks] = {
    seqJSON.map(x => {
      val unit = decodeBase64((x \ "key").as[String]).split("~").last
      UnitLinks(id, extractParents(unit, jsToUnitMap(x)), extractChildren(unit, jsToUnitMap(x)), unit)
    }).toList
  }

  def singleGETRequest(path: String, headers: Seq[(String, String)] = Seq(), params: Seq[(String, String)] = Seq()): Future[WSResponse] = {
    logger.error(s"path: ${path}")
    logger.error(s"headers: ${headers}")
    logger.error(s"params: ${params}")
    ws.url(path.toString)
      .withQueryString(params: _*)
      .withHeaders(headers: _*)
      .get
  }

  def extractParents(key: String, map: Map[String, String]): Option[Map[String, String]] = key match {
    case "ENT" => None
    case "LEU" => Some(map.filterKeys(_ == "ENT"))
    case _ => Some(map filterKeys Set("LEU", "ENT"))
  }

  def extractChildren(key: String, map: Map[String, String]): Option[Map[String, String]] = key match {
    case "ENT" => Some(map)
    case "LEU" => Some(map - "ENT")
    case _ => None
  }

  private def jsToUnitMap(js: JsValue): Map[String, String] = {
    (js \ "Cell").as[Seq[JsValue]].map { cell =>
      val column = decodeBase64((cell \ "column").as[String])
        .split(":", columnFamilyAndValueSubstring).last
        .split("_").last
      val value = decodeBase64((cell \ "$").as[String])
      column -> value
    }.toMap
  }

  private def jsToEntMap(js: JsValue): Map[String, String] = {
    // An enterprise id is unique so we can safely always get the first JS value
    (js(0) \ "Cell").as[Seq[JsValue]].map { cell =>
      val column = decodeBase64((cell \ "column").as[String]).split(":", columnFamilyAndValueSubstring).last
      val value = decodeBase64((cell \ "$").as[String])
      column -> value
    }.toMap
  }

  private def convertToUnitMap(result: JsValue): Map[String, String] = {
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

  def encodeBase64(str: Seq[String], deliminator: String = ":"): String =
    BaseEncoding.base64.encode(str.mkString(deliminator).getBytes(Charsets.UTF_8))

  def decodeBase64(str: String): String = new String(BaseEncoding.base64().decode(str), "UTF-8")

  def createRowKey(period: String, id: String): String = String.join(DELIMITER, period, id)

  def createRowKey(period: String, id: String, unitType: Option[UnitType]): String = {
    unitType match {
      case Some(u) => String.join(DELIMITER, period, id, u.toString)
      case None => String.join(DELIMITER, period, id, "*")
    }
  }
}
