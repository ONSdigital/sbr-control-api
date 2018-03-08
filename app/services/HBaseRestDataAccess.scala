package services

import javax.inject.Inject

import com.netaporter.uri.dsl._
import com.typesafe.scalalogging.LazyLogging

import play.api.libs.ws.{ WSClient, WSResponse }
import play.api.http.Status
import play.api.Configuration
import play.api.libs.json.{ JsArray, JsLookupResult, JsValue }

import scala.concurrent.{ Await, Future }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import config.Properties
import uk.gov.ons.sbr.models._
import uk.gov.ons.sbr.models.units.{ Child, EnterpriseUnit, LEU, UnitLinks }
import utils.Utilities._

// TODO:
// - when creating the childrenJSON, blocking code is used to resolve the Future, rather than
//   using a Future within a case class, this may not be the best way to do it
// - there are quite a few repeated .split("~").last, this could be put in a function
// - add logs for errors when hitting hbase REST
/**
 * Created by coolit on 05/02/2018.
 */
class HBaseRestDataAccess @Inject() (ws: WSClient, val configuration: Configuration) extends DataAccess with Properties with LazyLogging {

  private val columnFamilyAndValueSubstring: Int = 2
  private val AUTH = encodeBase64(Seq(username, password))
  private val HEADERS = Seq("Accept" -> "application/json", "Authorization" -> s"Basic $AUTH")

  def getUnitLinks(id: String): Future[DbResponse] = getStatAndUnitLinks[List[UnitLinks]](id, None, None, transformUnitSeqJson)

  def getStatUnitLinks(id: String, category: String, period: String): Future[DbResponse] = getStatAndUnitLinks[UnitLinks](id, Some(period), Some(category), transformStatSeqJson)

  def getEnterprise(id: String, period: Option[String]): Future[DbResponse] = {
    // HBase key format: 9901566115~201706, id~period
    val rowKey = createEntRowKey(period, id.reverse)
    val uri = baseUrl / enterpriseTableName.getNameAsString / rowKey / enterpriseColumnFamily
    logger.info(s"Using URI [$uri] for getEnterprise")
    logger.debug(s"Sending GET request to HBase REST for enterprise using rowKey [$rowKey]")
    singleGETRequest(uri.toString, HEADERS).map(x => handleWsResponse(id, period, x, handleDbResultEnt))
  }

  def getStatAndUnitLinks[T](id: String, period: Option[String], unitType: Option[String], transformJson: (String, Seq[JsValue], JsValue) => T): Future[DbResponse] = {
    // HBase key format: 201706~01752564~CH, period~id~type
    // When there is no unitType, * is used to get rows of any unit type
    val rowKey = createUnitLinksRowKey(id, period, unitType)
    val uri = baseUrl / unitTableName.getNameAsString / rowKey / unitLinksColumnFamily
    logger.info(s"Using URI [$uri] for getStatAndUnitLinks")
    logger.debug(s"Sending GET request to HBase REST for unit links using rowKey [$rowKey]")
    singleGETRequest(uri.toString, HEADERS).map(x => handleWsResponse(id, period, x, handleDbResultUnit))
  }

  def handleWsResponse(id: String, period: Option[String], ws: WSResponse, f: (String, Option[String], WSResponse) => DbResponse): DbResponse = ws match {
    case response if response.status == Status.OK => f(id, period, response)
    case response if response.status == Status.NOT_FOUND => DbNotFound()
    case response if response.status == Status.INTERNAL_SERVER_ERROR => DbServerError()
    case response if response.status == Status.SERVICE_UNAVAILABLE => DbServiceUnavailable()
    case response if response.status == Status.REQUEST_TIMEOUT => DbTimeout()
  }

  def handleDbResultEnt(id: String, period: Option[String], response: WSResponse): DbResponse = {
    val row = (response.json \ "Row").as[JsValue]
    // If the period is present, we use the first result, as using id~period as the row key will get one result
    // For the most recent period (i.e. period is None), we need to use the last result (Due to how the HBase
    // REST API scan works (if we could do a reverse scan we'd only have to get the first result)
    period match {
      case Some(p) => DbResult(EnterpriseUnit(id, p, jsToEntMap(row(0)), "ENT", createEnterpriseChildJSON(id, p)))
      case None => {
        // We need to get the period from the HBase row key
        val keyPeriod = decodeBase64((row.last \ "key").as[String]).split("~").last
        DbResult(EnterpriseUnit(id, keyPeriod, jsToEntMap(row.last), "ENT", createEnterpriseChildJSON(id, keyPeriod)))
      }
    }
  }

  def handleDbResultUnit(id: String, period: Option[String], response: WSResponse): DbResponse = {
    val row = (response.json \ "Row").as[JsValue]
    val seqJSON = row.as[Seq[JsValue]]
    // Depending on whether the unitType is present or not, we use a different function to transform the JSON
    // from HBase, either returning UnitLinks (when unitType is present) or List[UnitLinks] when there is
    // no unitType present
    period match {
      case Some(_) => DbResult[UnitLinks](transformStatSeqJson(id, seqJSON, row))
      case None => DbResult[List[UnitLinks]](transformUnitSeqJson(id, seqJSON, row))
    }
  }

  /**
   * Whenever an enterprise is returned, a field called childrenJSON in the model needs to be populated.
   * This will involve getting the unit links for that particular enterprise and forming a tree of nested
   * JSON of each child, and any children of that child. Example:
   *
   * {
   *   "childrenJson": [
   *     {
   *       "type": "LEU",
   *       "id": "12345",
   *       "children": [
   *         { "type": "CH", "id": "5325" },
   *         { "type": "VAT", "id": "2947" },
   *       ]
   *     },
   *    ...
   *   ]
   * }
   *
   */
  def createEnterpriseChildJSON(entId: String, period: String): List[LEU] = {
    logger.info(s"Creating child JSON for enterprise [$entId] with period [$period]")
    val unitLinks = getStatUnitLinks(entId, "ENT", period)
    // @TODO: The await is a temporary measure to use whilst testing
    Await.result(unitLinks, 2 seconds) match {
      case a: DbResult[UnitLinks] => a.result.children match {
        case Some(c) => {
          // Now that we have the unitLinks and we know that there are items in this list,
          // we need to form the correct JSON format using the Map[String, String] which is
          // a map of Map[id, unitType]. We can ignore the unitType's that are at the bottom
          // of the hierarchy (VAT, PAYE, CH) as we do not know which of the LEUs is their parent.
          val leus = c.filter(_._2 == "LEU").keySet.toList
          leus.map(x => LEU("LEU", x, getChildrenForLEU(x, period)))
        }
        case None => List()
      }
    }
  }

  def getChildrenForLEU(childId: String, period: String): List[Child] = {
    val unitLinks = getStatUnitLinks(childId, "LEU", period)
    // @TODO: The await is a temporary measure to use whilst testing
    Await.result(unitLinks, 2 seconds) match {
      case a: DbResult[UnitLinks] => a.result.children match {
        case Some(c) => c.map(x => Child(x._2, x._1)).toList
        case None => List()
      }
      case _ => List()
    }
  }

  def transformStatSeqJson(id: String, seqJSON: Seq[JsValue], row: JsValue): UnitLinks = {
    val unitType = decodeBase64((seqJSON(0) \ "key").as[String]).split("~").tail.head
    UnitLinks(id, extractParents(unitType, convertToUnitMap(row)), extractChildren(unitType, convertToUnitMap(row)), unitType)
  }

  // Get every bit of data for the most recent period
  def transformUnitSeqJson(id: String, seqJSON: Seq[JsValue], row: JsValue): List[UnitLinks] = {
    val period = decodeBase64((seqJSON.last \ "key").as[String]).split("~").last
    // We only want the most recent period
    val filteredJSON = seqJSON.filter(x => decodeBase64((x \ "key").as[String]).split("~").last == period)
    filteredJSON.map(x => {
      val unitType = decodeBase64((x \ "key").as[String]).split("~").tail.head
      UnitLinks(id, extractParents(unitType, jsToUnitMap(x)), extractChildren(unitType, jsToUnitMap(x)), unitType)
    }).toList
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

  def singleGETRequest(path: String, headers: Seq[(String, String)] = Seq(), params: Seq[(String, String)] = Seq()): Future[WSResponse] = {
    ws.url(path.toString)
      .withQueryString(params: _*)
      .withHeaders(headers: _*)
      .get
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

  private def jsToEntMap(js: JsLookupResult): Map[String, String] = {
    // An enterprise id is unique so we can safely always get the first JS value
    (js \ "Cell").as[Seq[JsValue]].map { cell =>
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
}
