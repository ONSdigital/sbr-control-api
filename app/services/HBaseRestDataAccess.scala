package services

import javax.inject.Inject
import java.util.Optional

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import org.joda.time.YearMonth
import org.joda.time.format.DateTimeFormat
import play.api.Configuration
import utils.FutureResponse.{ futureSuccess }

import play.api.libs.json.{ JsArray, JsValue }
import play.api.libs.ws.WSResponse
import play.api.libs.ws.ning.NingWSClient
import play.api.mvc.{ AnyContent, Request, Result }
import utils.HBaseConfig

import com.netaporter.uri.dsl._
import uk.gov.ons.sbr.models.units.EnterpriseUnit
import utils.Utilities._

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }

/**
 * Created by coolit on 05/02/2018.
 */
class HBaseRestDataAccess @Inject() (val configuration: Configuration) extends DataAccess with HBaseConfig {

  // WSClient should be injected in, however due to a dependency issue this was not possible, this will be fixed
  // before this branch is merged.

  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  val ws = NingWSClient()
  //wsClient.url("http://wwww.google.com").get()

  //at the very end, to shutdown stuff cleanly :
  //wsClient.close()
  //actorSystem.terminate()

  //HBaseInMemoryConfig

  val REFERENCE_PERIOD_FORMAT = "yyyyMM" //configuration.getString("db.period.format").getOrElse("yyyyMM")
  private val AUTH = encodeBase64(Seq("username", "password"))
  private val HEADERS = Seq("Accept" -> "application/json", "Authorization" -> s"Basic $AUTH")

  def getUnitLinksFromDB(id: String)(implicit request: Request[AnyContent]): Future[Result] = ???

  def getUnitLinksFromDB(id: String, period: String)(implicit request: Request[AnyContent]): Future[Result] = ???

  def getEnterpriseFromDB(id: String)(implicit request: Request[AnyContent]): Future[Result] = {
    getEnterprise(id, None) flatMap { x =>
      x match {
        case Some(s) => resultMatcher[EnterpriseUnit](Optional.ofNullable(s))
        case None => NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${id} and period xyz")).future
      }
    }
  }

  def getEnterpriseFromDB(id: String, period: String)(implicit request: Request[AnyContent]): Future[Result] = ???

  def getStatUnitLinkFromDB(id: String, category: String)(implicit request: Request[AnyContent]): Future[Result] = ???

  def getStatUnitLinkFromDB(id: String, period: String, category: String)(implicit request: Request[AnyContent]): Future[Result] = ???

  def singleGETRequest(path: String, headers: Seq[(String, String)] = Seq(), params: Seq[(String, String)] = Seq()): Future[WSResponse] =
    ws.url(path.toString)
      .withQueryString(params: _*)
      .withHeaders(headers: _*)
      .get

  def getTest(): Unit = {
    println("getting...")
  }

  def getEnterprise(id: String, period: Option[YearMonth]): Future[Option[EnterpriseUnit]] = {
    val period = YearMonth.parse("201706", DateTimeFormat.forPattern(REFERENCE_PERIOD_FORMAT))
    val rowKey = createRowKey(period, id)
    val uri = baseUrl / tableName.getNameWithNamespaceInclAsString / rowKey / columnFamily
    singleGETRequest(uri.toString, HEADERS) map {
      case response if response.status == OK => {
        println(s"response is (OK): ${response}")
        val resp = (response.json \ "Row").as[JsValue]
        println(s"resp is ... ${resp}")
        //val key = (resp \ "key").as[Long]
        val vars = convertToEntMap(resp)
        val ent = EnterpriseUnit(id.toLong, period.toString, vars, "ENT", List())
        Some(ent)
      }
      case response if response.status == NOT_FOUND => {
        println(s"response is (NOT FOUND): ${response}")
        None
      }
    }
  }

  private def convertToEntMap(result: JsValue): Map[String, String] = {
    val js = result.as[JsArray]
    // result looks like the following:
    //      [ {
    //        "key" : "MjAxNzA2fjk5MDAxNTYxMTU=",
    //        "Cell" : [ {
    //        "column" : "ZDpOdW1fVW5pcXVlX1BheWVSZWZz",
    //        "timestamp" : 1517844752615,
    //        "$" : "MQ=="
    //      }, {
    //        "column" : "ZDpOdW1fVW5pcXVlX1ZhdFJlZnM=",
    //        "timestamp" : 1517844752615,
    //        "$" : "MQ=="
    //      } ...
    //     ]
    val columnFamilyAndValueSubstring = 2
    (js(0) \ "Cell").as[Seq[JsValue]].map { cell =>
      val column = decodeBase64((cell \ "column").as[String]).split(":", columnFamilyAndValueSubstring).last
      val value = decodeBase64((cell \ "$").as[String])
      column -> value
    }.toMap
  }

  def encodeBase64(str: Seq[String], deliminator: String = ":"): String =
    BaseEncoding.base64.encode(str.mkString(deliminator).getBytes(Charsets.UTF_8))

  def decodeBase64(str: String): String = new String(BaseEncoding.base64().decode(str), "UTF-8")

  val DELIMITER = "~"

  def createRowKey(referencePeriod: YearMonth, id: String): String =
    String.join(DELIMITER, referencePeriod.toString("yyyyMM"), id)
}
