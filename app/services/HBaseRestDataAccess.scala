package services

import javax.inject.Inject

import com.netaporter.uri.dsl._
import com.typesafe.scalalogging.LazyLogging

import play.api.libs.ws.{ WSAuthScheme, WSClient, WSResponse }
import play.api.http.Status
import play.api.Configuration
import play.api.libs.json.JsValue

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import config.Properties
import uk.gov.ons.sbr.models._
import uk.gov.ons.sbr.models.units.{ Child, EnterpriseUnit, LEU, UnitLinks }
import utils.HBaseRestUtils._

// TODO:
// - when creating the childrenJSON, blocking code is used to resolve the Future, rather than
//   using a Future within a case class, this may not be the best way to do it
// - there are quite a few repeated .split("~").last, this could be put in a function
// - add logs for errors when hitting hbase REST
/**
 * Created by coolit on 05/02/2018.
 */
class HBaseRestDataAccess @Inject() (ws: WSClient, val configuration: Configuration) extends DataAccess with Properties with LazyLogging {

  private val HEADERS = Seq("Accept" -> "application/json")

  val ENT_UNIT = "ENT"
  val LEU_UNIT = "LEU"
  val UNIT = "UNIT"

  def getUnitLinks(id: String): Future[DbResponse] = getStatAndUnitLinks(id, None, None)

  def getStatUnitLinks(id: String, category: String, period: String): Future[DbResponse] = getStatAndUnitLinks(id, Some(period), Some(category))

  def getEnterprise(id: String, period: Option[String]): Future[DbResponse] = {
    // HBase key format: 9901566115~201706, id~period
    val rowKey = createEntRowKey(period, id.reverse)
    val uri = baseUrl / enterpriseTableName.getNameAsString / rowKey / enterpriseColumnFamily
    logger.info(s"Getting Enterprise from HBase REST using URI [$uri]")
    singleGETRequest(uri.toString, HEADERS).map(x => handleWsResponse(ENT_UNIT, id, period, x, handle))
  }

  def getStatAndUnitLinks(id: String, period: Option[String], unitType: Option[String]): Future[DbResponse] = {
    // HBase key format: 201706~01752564~CH, period~id~type
    // When there is no unitType, * is used to get rows of any unit type
    val rowKey = createUnitLinksRowKey(id, period, unitType)
    val uri = baseUrl / unitTableName.getNameAsString / rowKey / unitLinksColumnFamily
    logger.info(s"Getting UnitLinks from HBase REST using URI [$uri]")
    singleGETRequest(uri.toString, HEADERS).map(x => handleWsResponse(UNIT, id, period, x, handle))
  }

  def handleWsResponse(unitType: String, id: String, period: Option[String], ws: WSResponse, f: (String, String, Option[String], WSResponse) => DbResponse): DbResponse = ws match {
    case response if response.status == Status.OK => f(unitType, id, period, response)
    case response if response.status == Status.NOT_FOUND => DbNotFound()
    case response if response.status == Status.INTERNAL_SERVER_ERROR => DbServerError()
    case response if response.status == Status.SERVICE_UNAVAILABLE => DbServiceUnavailable()
    case response if response.status == Status.REQUEST_TIMEOUT => DbTimeout()
  }

  def handle(unitType: String, id: String, period: Option[String], response: WSResponse): DbResponse = {
    // If the period is present, we use the first result, as using id~period as the row key will get one result
    // For the most recent period (i.e. period is None), we need to use the last result (Due to how the HBase
    // REST API scan works (if we could do a reverse scan we'd only have to get the first result)
    val row = (response.json \ "Row").as[Seq[JsValue]]
    unitType match {
      case ENT_UNIT => period match {
        case Some(p) => DbSuccessEnterprise(EnterpriseUnit(id, p, jsonToMap(row.last), ENT_UNIT, createEnterpriseChildJSON(id, p)))
        case None => {
          // We need to get the period from the HBase row key
          val keyPeriod = decodeBase64((row.last \ "key").as[String]).split(delimiter).last
          DbSuccessEnterprise(EnterpriseUnit(id, keyPeriod, jsonToMap(row.head), ENT_UNIT, createEnterpriseChildJSON(id, keyPeriod)))
        }
      }
      case "UNIT" => period match {
        case Some(_) => DbSuccessUnitLinks(transformUnitJson(id, row).last)
        case None => DbSuccessUnitLinksList(transformUnitJson(id, row))
      }
    }
  }

  /**
   * Whenever an enterprise is returned, a field called childrenJSON in the model needs to be populated.
   * This will involve getting the unit links for that particular enterprise and forming a tree of nested
   * JSON of each child, and any children of that child. Example:
   *
   * {
   *   "childrenJson": [
   *      {"type": "LEU", "id": "12345", "children": [{ "type": "CH", "id": "5325" }] }
   *   ]
   * }
   */
  def createEnterpriseChildJSON(entId: String, period: String): List[LEU] = {
    logger.info(s"Creating child JSON for enterprise [$entId] with period [$period]")
    val unitLinks = getStatUnitLinks(entId, ENT_UNIT, period)
    // @TODO: The await is a temporary measure to use whilst testing
    Await.result(unitLinks, 2 seconds) match {
      case a: DbSuccessUnitLinks => a.result.children match {
        case Some(c) => {
          // Now that we have the unitLinks and we know that there are items in this list,
          // we need to form the correct JSON format using the Map[String, String] which is
          // a map of Map[id, unitType]. We can ignore the unitType's that are at the bottom
          // of the hierarchy (VAT, PAYE, CH) as we do not know which of the LEUs is their parent.
          val leus = c.filter(_._2 == LEU_UNIT).keySet.toList
          leus.map(x => LEU(LEU_UNIT, x, getChildrenForLEU(x, period)))
        }
        case None => List()
      }
    }
  }

  def getChildrenForLEU(childId: String, period: String): List[Child] = {
    val unitLinks = getStatUnitLinks(childId, "LEU", period)
    // @TODO: The await is a temporary measure to use whilst testing
    Await.result(unitLinks, 2 seconds) match {
      case a: DbSuccessUnitLinks => a.result.children match {
        case Some(c) => c.map(x => Child(x._2, x._1)).toList
        case None => List()
      }
      case _ => List()
    }
  }

  def transformUnitJson(id: String, seqJSON: Seq[JsValue]): List[UnitLinks] = {
    val period = decodeBase64((seqJSON.last \ "key").as[String]).split(delimiter).last
    // We only want the most recent period
    val filteredJSON = seqJSON.filter(x => decodeBase64((x \ "key").as[String]).split(delimiter).last == period)
    filteredJSON.map(x => {
      val unitType = decodeBase64((x \ "key").as[String]).split(delimiter).tail.head
      UnitLinks(id, extractParents(unitType, jsonToMap(x)), extractChildren(unitType, jsonToMap(x)), unitType)
    }).toList
  }

  def singleGETRequest(path: String, headers: Seq[(String, String)] = Seq.empty): Future[WSResponse] = ws.url(path.toString)
    .withHeaders(headers: _*)
    .withAuth(username, password, WSAuthScheme.BASIC)
    .withRequestTimeout(timeout milliseconds)
    .get
}
