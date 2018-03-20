package services

import javax.inject.Inject

import com.netaporter.uri.dsl._
import com.typesafe.scalalogging.LazyLogging

import play.api.libs.ws.{ WSClient, WSResponse }
import play.api.http.Status
import play.api.Configuration
import play.api.libs.json.JsValue

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import config.Properties
import uk.gov.ons.sbr.models._
import uk.gov.ons.sbr.models.units.{ Child, EnterpriseUnit, EnterpriseHistoryUnit, LEU, UnitLinks }
import utils.HBaseRestUtils

// TODO:
// - when creating the childrenJSON, blocking code is used to resolve the Future, rather than
//   using a Future within a case class, this may not be the best way to do it
// - there are quite a few repeated .split("~").last, this could be put in a function
// - add logs for errors when hitting hbase REST
/**
 * Created by coolit on 05/02/2018.
 */
class HBaseRestDataAccess @Inject() (ws: WSClient, val configuration: Configuration) extends DataAccess with Properties with LazyLogging {

  private val utils = new HBaseRestUtils(ws, configuration)

  private val HEADERS = Seq("Accept" -> "application/json")

  def getUnitLinks(id: String): Future[DbResponse] = getStatAndUnitLinks(id, None, None)

  def getStatUnitLinks(id: String, category: String, period: String): Future[DbResponse] = getStatAndUnitLinks(id, Some(period), Some(category))

  //for ent history
  def getStatUnitLinksHistory(id: String, category: String, period: String): Future[DbResponse] = getStatAndUnitLinksHistory(id, Some(period), Some(category))

  def getEnterprise(id: String, period: Option[String]): Future[DbResponse] = {
    // HBase key format: 9901566115~201706, id~period
    val rowKey = utils.createEntRowKey(period, id.reverse)
    val uri = baseUrl / enterpriseTableName.getNameAsString / rowKey / enterpriseColumnFamily
    logger.info(s"Getting Enterprise from HBase REST using URI [$uri]")
    utils.singleGETRequest(uri.toString, HEADERS).map(x => handleWsResponse(id, period, x, handleEntResponse))
  }
  //for ent history
  def getEnterpriseHistory(id: String, period: Option[Int]): Future[DbResponse] = {
    // HBase key format: 9901566115/1, id/history
    //val rowKey = utils.createEntHistoryRowKey(period, id)
    val rowKey = utils.createEntRowKey(None, id.reverse)
    val uri = baseUrl / enterpriseTableName.getNameAsString / rowKey / enterpriseColumnFamily
    //val uri = baseUrl / enterpriseTableName.getNameAsString / rowKey / "history"
    //val uri = baseUrl / "v1" / "enterprise" / rowKey / "history"
    logger.info(s"Getting Enterprise from HBase REST using URI [$uri]")
    utils.singleGETRequest(uri.toString, HEADERS).map(x => handleWsResponseHistory(id, period, x, handleEntHistoryResponse))
  }

  def getStatAndUnitLinks(id: String, period: Option[String], unitType: Option[String]): Future[DbResponse] = {
    // HBase key format: 201706~01752564~CH, period~id~type
    // When there is no unitType, * is used to get rows of any unit type
    val rowKey = utils.createUnitLinksRowKey(id, period, unitType)
    val uri = baseUrl / unitTableName.getNameAsString / rowKey / unitLinksColumnFamily
    logger.info(s"3 Getting UnitLinks from HBase REST using URI [$uri]")
    utils.singleGETRequest(uri.toString, HEADERS).map(x => handleWsResponse(id, period, x, handleLinksResponse))
  }

  def getStatAndUnitLinksHistory(id: String, period: Option[String], unitType: Option[String]): Future[DbResponse] = {
    // HBase key format: 201706~01752564~CH, period~id~type
    // When there is no unitType, * is used to get rows of any unit type
    val rowKey = utils.createUnitLinksRowKey(id, period, unitType)
    val uri = baseUrl / unitTableName.getNameAsString / rowKey / unitLinksColumnFamily
    logger.info(s"4 Getting UnitLinks from HBase REST using URI [$uri]")
    utils.singleGETRequest(uri.toString, HEADERS).map(x => handleWsResponse(id, period, x, handleLinksResponse))
  }

  /**
   * If the status of the WsResponse is OK, use the passed in wsToDbResponse function to transform the response
   * into the correct DbResponse format.
   */
  def handleWsResponse(id: String, period: Option[String], ws: WSResponse, wsToDbResponse: (String, Option[String], WSResponse) => DbResponse): DbResponse = ws match {
    case response if response.status == Status.OK => wsToDbResponse(id, period, response)
    case response if response.status == Status.NOT_FOUND => DbNotFound()
    case response if response.status == Status.INTERNAL_SERVER_ERROR => DbServerError()
    case response if response.status == Status.SERVICE_UNAVAILABLE => DbServiceUnavailable()
    case response if response.status == Status.REQUEST_TIMEOUT => DbTimeout()
  }
  //made new handleWsResponse fot the history with period: Option[Int]
  def handleWsResponseHistory(id: String, period: Option[Int], ws: WSResponse, wsToDbResponse: (String, Option[Int], WSResponse) => DbResponse): DbResponse = ws match {
    case response if response.status == Status.OK => wsToDbResponse(id, period, response)
    case response if response.status == Status.NOT_FOUND => DbNotFound()
    case response if response.status == Status.INTERNAL_SERVER_ERROR => DbServerError()
    case response if response.status == Status.SERVICE_UNAVAILABLE => DbServiceUnavailable()
    case response if response.status == Status.REQUEST_TIMEOUT => DbTimeout()
  }

  /**
   * When we pass the JsLookupResult to jsonToMap, the index does not matter for exact matches (wherever the full row key
   * is used without using the "*"). For no period, where the id is id~*, the default HTTP request does a normal
   * scan, so we need to get the last item in the Json array, which will be the most recent.
   */
  def handleEntResponse(id: String, period: Option[String], response: WSResponse): DbResponse = {
    val row = (response.json \ "Row").as[Seq[JsValue]]
    period match {
      case Some(p) => DbSuccessEnterprise(EnterpriseUnit(id, p, utils.jsonToMap(row.head, utils.formEntKey), entUnit, createEnterpriseChildJSON(id, p)))
      case None => {
        // We do not know what the period is so we need to get it from the HBase row key
        val keyPeriod = utils.decodeBase64((row.last \ "key").as[String]).split(delimiter).last
        DbSuccessEnterprise(EnterpriseUnit(id, keyPeriod, utils.jsonToMap(row.last, utils.formEntKey), entUnit, createEnterpriseChildJSON(id, keyPeriod)))
      }
    }
  }

  def handleEntHistoryResponse(id: String, max: Option[Int], response: WSResponse): DbResponse = {
    val row = (response.json \ "Row").as[Seq[JsValue]]
    //DbSuccessEnterpriseHistory(row.map(x => x))
    max match {
      case Some(m) => {
        val allEnts = row.map(x => {
          val period = utils.decodeBase64((x \ "key").as[String]).split(delimiter).last
          EnterpriseUnit(id, period, utils.jsonToMap(row.head, utils.formEntKey), entUnit, createEnterpriseChildJSON(id, period))
        })
        DbSuccessEnterpriseHistory(allEnts.toList.reverse.slice(0, m))
      }
      case None => {
        val allEnts = row.map(x => {
          val period = utils.decodeBase64((x \ "key").as[String]).split(delimiter).last
          EnterpriseUnit(id, period, utils.jsonToMap(row.head, utils.formEntKey), entUnit, createEnterpriseChildJSON(id, period))
        })
        DbSuccessEnterpriseHistory(allEnts.toList)
      }
    }
  }

  /**
   * When we have a full row key (id~unitType~period), an exact match will be returned so it does not matter which
   * index of the returned JSON we use, with an incomplete row key (id~*), multiple results will be returned.
   */
  def handleLinksResponse(id: String, period: Option[String], response: WSResponse): DbResponse = {
    val row = (response.json \ "Row").as[Seq[JsValue]]
    period match {
      case Some(_) => DbSuccessUnitLinks(transformUnitJson(id, row).head)
      case None => DbSuccessUnitLinksList(transformUnitJson(id, row))
    }
  }
  def handleLinksResponseHistory(id: String, period: Option[Int], response: WSResponse): DbResponse = {
    val row = (response.json \ "Row").as[Seq[JsValue]]
    period match {
      case Some(_) => DbSuccessUnitLinks(transformUnitJson(id, row).head)
      case None => DbSuccessUnitLinksList(transformUnitJson(id, row))
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
    val unitLinks = getStatUnitLinks(entId, entUnit, period)
    // @TODO: The await is a temporary measure to use whilst testing
    // @TODO: Add better handling of errors
    Await.result(unitLinks, 2 seconds) match {
      case a: DbSuccessUnitLinks => a.result.children match {
        case Some(childMap) => {
          // Now that we have the unitLinks and we know that there are items in this list,
          // we need to form the correct JSON format using the Map[String, String] which is
          // a map of Map[id, unitType].
          childMap
            .filter(_._2 == leuUnit)
            .keySet.toList
            .map(id => LEU(leuUnit, id, getChildrenForLEU(id, period)))
        }
        case None => List()
      }
      case _ => List()
    }
  }

  def getChildrenForLEU(childId: String, period: String): List[Child] = {
    val unitLinks = getStatUnitLinks(childId, "LEU", period)
    // @TODO: The await is a temporary measure to use whilst testing
    Await.result(unitLinks, 2 seconds) match {
      case a: DbSuccessUnitLinks => a.result.children match {
        case Some(c) => c.map { case (x, y) => Child(x, y) }.toList
        case None => List()
      }
      case _ => List()
    }
  }

  /**
   * Given a Seq[JsValue], traverse the sequence and create UnitLinks for each item.
   */
  def transformUnitJson(id: String, seqJSON: Seq[JsValue]): List[UnitLinks] = {
    val period = utils.decodeBase64((seqJSON.last \ "key").as[String]).split(delimiter).last
    // We only want the most recent period
    val filteredJSON = seqJSON.filter(x => utils.decodeBase64((x \ "key").as[String]).split(delimiter).last == period)
    filteredJSON.map(x => {
      val unitType = utils.decodeBase64((x \ "key").as[String]).split(delimiter).tail.head
      UnitLinks(
        id,
        utils.extractParents(unitType, utils.jsonToMap(x, utils.formUnitKey)),
        utils.extractChildren(unitType, utils.jsonToMap(x, utils.formUnitKey)),
        unitType
      )
    }).toList
  }
}
