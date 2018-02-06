package services

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import org.joda.time.YearMonth
import org.joda.time.format.DateTimeFormat
import play.api.Configuration
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future, Promise, Await }
import play.api.http.Status
import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse
import play.api.libs.ws.ning.NingWSClient
import play.api.mvc.{ AnyContent, Request, Result }
import uk.gov.ons.sbr.data.hbase.util.RowKeyUtils
import utils.HBaseConfig

import scala.concurrent.duration.Duration
import com.netaporter.uri.dsl._
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }
import scala.util.{ Failure, Success, Try }

/**
 * Created by coolit on 05/02/2018.
 */
class HBaseRestDataAccess @Inject() (val configuration: Configuration) extends DataAccess with HBaseConfig {

  // WSClient should be injected in, however due to a dependency issue this was not possible, this will be fixed
  // before this branch is merged.

  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
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
    println("getting from db...")
    val rowKey = createRowKey(YearMonth.parse("201706", DateTimeFormat.forPattern(REFERENCE_PERIOD_FORMAT)), id)
    val uri = "http://localhost:8080" / tableName.getNameWithNamespaceInclAsString / rowKey / columnFamily
    val resp = singleGETRequest(uri.toString, HEADERS)
    Await.result(resp, 5000 millisecond)
    println(s"resp is: ${resp}")
    println(s"uri is: ${uri}")
    throw new IndexOutOfBoundsException
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

  //  def get(period: YearMonth, id: String): Unit = {
  //    val rowKey = createRowKey(period, id)
  //    val uri = baseUrl / tableName.getNameWithNamespaceInclAsString / rowKey / columnFamily
  //    ws.singleGETRequest(uri.toString, HEADERS)
  //  }

  //  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  //  private val AUTH = EncodingUtil.encodeBase64(Seq(username, password))
  //  private val HEADERS = Seq("Accept" -> "application/json", "Authorization" -> s"Basic $AUTH")
  //
  //  // TODO - add Circuit breaker
  //  override def lookup(referencePeriod: Option[YearMonth], key: String, max: Option[Long]): Future[Option[Seq[AdminData]]] =
  //    getAdminData(referencePeriod, key, max)
  //
  //  @throws(classOf[Throwable])
  //  private def getAdminData(referencePeriod: Option[YearMonth], key: String, max: Option[Long]): Future[Option[Seq[AdminData]]] = {
  //    val default: Int = 1
  //    (referencePeriod match {
  //      case Some(r: YearMonth) =>
  //          val rowKey = RowKeyUtils.createRowKey(r, key)
  //          val uri = baseUrl / tableName.getNameWithNamespaceInclAsString / rowKey / columnFamily
  //        LOGGER.debug(s"Making restful GET request to HBase with url path ${uri.toString} and headers ${HEADERS.head.toString}")
  //        ws.singleGETRequest(uri.toString, HEADERS)
  //      case None =>
  //        //        val params = if (max.isDefined) {
  //        //          Seq("reversed" -> "true", "limit" -> max.get.toString)
  //        //        } else {
  //        //          Seq("reversed" -> "true")
  //        //        }
  //        val uri = baseUrl / tableName.getNameWithNamespaceInclAsString / key + RowKeyUtils.DELIMITER + "*"
  //        LOGGER.debug(s"Making restful SCAN request to HBase with url ${uri.toString}, headers ${HEADERS.head.toString} ")
  //        ws.singleGETRequest(uri.toString, HEADERS)
  //    }).map {
  //      case response if response.status == OK => {
  //        val resp = (response.json \ "Row").as[Seq[JsValue]]
  //        Try(resp.map(v => convertToAdminData(v))) match {
  //          case Success(adminData: Seq[AdminData]) =>
  //            LOGGER.debug("Found data for prefix row key '{}'", key)
  //            //            Some(adminData)
  //            // @NOTE - all responses need to be in DESC and capped - GET is LIMIT 1 thus result to no effect
  //            Some(adminData.reverse.take(max.getOrElse(default).toString.toInt))
  //          case Failure(e: Throwable) =>
  //            LOGGER.error(s"Error getting data for row key $key", e)
  //            throw e
  //        }
  //      }
  //      case response if response.status == NOT_FOUND =>
  //        LOGGER.debug("No data found for prefix row key '{}'", key)
  //        None
  //    }
  //  }
  //
  //  private def convertToAdminData(result: JsValue): AdminData = {
  //    val columnFamilyAndValueSubstring = 2
  //    val key = (result \ "key").as[String]
  //    LOGGER.debug(s"Found record $key")
  //    val adminData: AdminData = RowKeyUtils.createAdminDataFromRowKey(decodeBase64(key))
  //    val varMap = (result \ "Cell").as[Seq[JsValue]].map { cell =>
  //      val column = decodeBase64((cell \ "column").as[String]).split(":", columnFamilyAndValueSubstring).last
  //      val value = decodeBase64((cell \ "$").as[String])
  //      column -> value
  //    }.toMap
  //    val newPutAdminData = adminData.putVariable(varMap)
  //    newPutAdminData
  //  }

  //  def decodeArrayByte(bytes: Array[Byte]): String = bytes.map(_.toChar).mkString
  //
  //  def encodeToArrayByte(str: String): Array[Byte] = str.getBytes("UTF-8")
  //
  def encodeBase64(str: Seq[String], deliminator: String = ":"): String =
    BaseEncoding.base64.encode(str.mkString(deliminator).getBytes(Charsets.UTF_8))
  //
  //  def decodeBase64(str: String): String =
  //    new String(BaseEncoding.base64().decode(str), "UTF-8")
  //
  val DELIMITER = "~"
  //
  def createRowKey(referencePeriod: YearMonth, id: String): String =
    String.join(DELIMITER, referencePeriod.toString("yyyyMM"), id)

  //  def createAdminDataFromRowKey(rowKey: String): AdminData = {
  //    val compositeRowKeyParts: Array[String] = rowKey.split(DELIMITER)
  //    val referencePeriod: YearMonth =
  //      YearMonth.parse(compositeRowKeyParts.last, DateTimeFormat.forPattern(REFERENCE_PERIOD_FORMAT))
  //    val id = compositeRowKeyParts.head
  //    AdminData(referencePeriod, id)
  //  }
}
