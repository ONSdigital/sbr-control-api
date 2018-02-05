package services

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import org.joda.time.YearMonth
import play.api.http.Status
import play.api.libs.json.JsValue
import play.api.mvc.{ AnyContent, Request, Result }

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }
import scala.util.{ Failure, Success, Try }

/**
 * Created by coolit on 05/02/2018.
 */
class HBaseRestDataAccess extends DataAccess {

  HBaseInMemoryConfig

  def getUnitLinksFromDB(id: String)(implicit request: Request[AnyContent]): Future[Result] = ???

  def getUnitLinksFromDB(id: String, period: String)(implicit request: Request[AnyContent]): Future[Result] = ???

  def getEnterpriseFromDB(id: String)(implicit request: Request[AnyContent]): Future[Result] = ???

  def getEnterpriseFromDB(id: String, period: String)(implicit request: Request[AnyContent]): Future[Result] = ???

  def getStatUnitLinkFromDB(id: String, category: String)(implicit request: Request[AnyContent]): Future[Result] = ???

  def getStatUnitLinkFromDB(id: String, period: String, category: String)(implicit request: Request[AnyContent]): Future[Result] = ???

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
  //        val rowKey = RowKeyUtils.createRowKey(r, key)
  //        val uri = baseUrl / tableName.getNameWithNamespaceInclAsString / rowKey / columnFamily
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
}

object EncodingUtil extends Status {

  def decodeArrayByte(bytes: Array[Byte]): String = bytes.map(_.toChar).mkString

  def encodeToArrayByte(str: String): Array[Byte] = str.getBytes("UTF-8")

  def encodeBase64(str: Seq[String], deliminator: String = ":"): String =
    BaseEncoding.base64.encode(str.mkString(deliminator).getBytes(Charsets.UTF_8))

  def decodeBase64(str: String): String =
    new String(BaseEncoding.base64().decode(str), "UTF-8")
}
