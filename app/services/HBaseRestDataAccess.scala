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
import utils.FutureResponse.futureSuccess
import play.api.libs.json.{ JsArray, JsValue }
import play.api.libs.ws.WSResponse
import play.api.libs.ws.ning.NingWSClient
import play.api.mvc.{ AnyContent, Request, Result }
import utils.{ HBaseConfig, IdRequest, ReferencePeriod, RequestEvaluation }
import com.netaporter.uri.dsl._
import uk.gov.ons.sbr.data.domain.Enterprise
import uk.gov.ons.sbr.models.units.EnterpriseUnit
import utils.Utilities._
import utils.FutureResponse.{ futureFromTry, futureSuccess }
import scala.concurrent.duration._

import scala.concurrent.{ Await, ExecutionContext, ExecutionContextExecutor, Future }
import scala.util.Try

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
  val DEFAULT_PERIOD = YearMonth.parse("201706", DateTimeFormat.forPattern(REFERENCE_PERIOD_FORMAT))
  private val AUTH = encodeBase64(Seq("username", "password"))
  private val HEADERS = Seq("Accept" -> "application/json", "Authorization" -> s"Basic $AUTH")

  def getUnitLinksFromDB(id: String)(implicit request: Request[AnyContent]): Future[Result] = ???

  def getUnitLinksFromDB(id: String, period: String)(implicit request: Request[AnyContent]): Future[Result] = ???

  def getEnterpriseFromDB(id: String)(implicit request: Request[AnyContent]): Future[Result] = {
    val evalResp = matchByParams(Some(id))
    search[EnterpriseUnit](evalResp, getEnterpriseById)
  }

  def getEnterpriseFromDB(id: String, period: String)(implicit request: Request[AnyContent]): Future[Result] = {
    val evalResp = matchByParams(Some(id), Some(period))
    searchByPeriod[EnterpriseUnit](evalResp, getEnterpriseByIdPeriod)
  }

  def getStatUnitLinkFromDB(id: String, category: String)(implicit request: Request[AnyContent]): Future[Result] = ???

  def getStatUnitLinkFromDB(id: String, period: String, category: String)(implicit request: Request[AnyContent]): Future[Result] = ???

  def singleGETRequest(path: String, headers: Seq[(String, String)] = Seq(), params: Seq[(String, String)] = Seq()): Future[WSResponse] =
    ws.url(path.toString)
      .withQueryString(params: _*)
      .withHeaders(headers: _*)
      .get

  def getEnterpriseById(id: String) = getEnterprise(id, None)
  def getEnterpriseByIdPeriod(period: YearMonth, id: String) = getEnterprise(id, Some(period))

  def getEnterprise(id: String, period: Option[YearMonth]): Optional[EnterpriseUnit] = {
    val rowKey = createRowKey(period.getOrElse(DEFAULT_PERIOD), id)
    val uri = baseUrl / tableName.getNameWithNamespaceInclAsString / rowKey / columnFamily
    val r = singleGETRequest(uri.toString, HEADERS) map {
      case response if response.status == OK => {
        val resp = (response.json \ "Row").as[JsValue]
        val vars = convertToEntMap(resp)
        val ent = EnterpriseUnit(id.toLong, period.getOrElse(DEFAULT_PERIOD).toString(REFERENCE_PERIOD_FORMAT), vars, "ENT", List())
        // Some(ent)
        Optional.ofNullable(ent)
      }
      case response if response.status == NOT_FOUND => Optional.empty[EnterpriseUnit]() // None
    }
    // The Await() is a temporary fix until the search method is updated to work with futures
    Await.result(r, 5000 millisecond)
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

  private def search[X](eval: RequestEvaluation, funcWithId: String => Optional[X]): Future[Result] = {
    val res = eval match {
      case (x: IdRequest) =>
        val resp = Try(funcWithId(x.id)).futureTryRes.flatMap {
          case (s: Optional[X]) => if (s.isPresent) {
            resultMatcher[X](s)
          } else NotFound(errAsJson(NOT_FOUND, "not_found", s"Could not find enterprise with id ${x.id}")).future
        } recover responseException
        resp
      case _ => invalidSearchResponses(eval)
    }
    res
  }

  //  //  //  @todo - combine ReferencePeriod and UnitGrouping
  private def searchByPeriod[X](eval: RequestEvaluation, funcWithIdAndParam: (YearMonth, String) => Optional[X]): Future[Result] = {
    val res = eval match {
      case (x: ReferencePeriod) => Try(funcWithIdAndParam(YearMonth.parse(x.period.toString.filter(_ != '-'), DateTimeFormat.forPattern(REFERENCE_PERIOD_FORMAT)), x.id)).futureTryRes.flatMap {
        case (s: Optional[X]) => if (s.isPresent) {
          resultMatcher[X](s)
        } else NotFound(errAsJson(NOT_FOUND, "not_found",
          s"Could not find enterprise with id ${x.id} and period ${x.period}")).future
      } recover responseException
      case _ => invalidSearchResponses(eval)
    }
    res
  }
}
