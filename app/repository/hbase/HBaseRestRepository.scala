package repository.hbase

import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import play.api.http.HeaderNames.{ ACCEPT, CONTENT_TYPE }
import play.api.http.MimeTypes.JSON
import play.api.http.Status.{ NOT_FOUND, NOT_MODIFIED, OK, UNAUTHORIZED }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.toJson
import play.api.libs.json.{ JsValue, Reads }
import play.api.libs.ws.WSAuthScheme.BASIC
import play.api.libs.ws.{ WSClient, WSRequest, WSResponse }
import repository.RestRepository.{ ErrorMessage, Field, Row, RowKey }
import repository._
import utils.{ BaseUrl, TrySupport }

import scala.concurrent.duration._
import scala.concurrent.{ Future, TimeoutException }
import scala.util.Try

case class HBaseRestRepositoryConfig(baseUrl: BaseUrl, namespace: String, username: String, password: String, timeout: Long)

/*
 * The public finder methods exposed by this class should be implemented in such a way as to always
 * return a successful Future, materialising any failure in the result value (on the left).
 * This simplifies client interaction.
 */
class HBaseRestRepository @Inject() (
    config: HBaseRestRepositoryConfig,
    wsClient: WSClient,
    responseReaderMaker: HBaseResponseReaderMaker
) extends RestRepository with LazyLogging {

  /*
   * Note that if performance is really an issue, the secondary Future generated by mapping over the result of findRows
   * can be eliminated via "map fusion".  In this case, by composing the function onto the end of the andThen chain in
   * findRows.  This would however require a private higher-order function to which both findRow & findRows could
   * delegate, and ultimately seemed less readable.
   */
  override def findRow(table: String, rowKey: String, columnFamily: String): Future[Either[ErrorMessage, Option[Row]]] =
    findRows(table, rowKey, columnFamily).map(verifyAtMostOneRow)

  override def findRows(table: String, query: String, columnFamily: String): Future[Either[ErrorMessage, Seq[Row]]] = {
    val withRowReader = responseReaderMaker.forColumnFamily(columnFamily)
    val url = HBase.rowKeyUrl(withBase = config.baseUrl, config.namespace, table, query, columnFamily)
    logger.info(s"Requesting [$url] from HBase REST.")

    baseRequest(url).withHeaders(ACCEPT -> JSON).get().map {
      (fromResponseToErrorOrJson _).andThen(convertToErrorOrRows(withRowReader))
    }.recover(withTranslationOfFailureToError)
  }

  private def baseRequest(url: String): WSRequest =
    wsClient.
      url(url).
      withAuth(config.username, config.password, scheme = BASIC).
      withRequestTimeout(config.timeout.milliseconds)

  /*
   * Note that official environments running Cloudera will receive an OK result containing an "empty row" on Not Found.
   * Developers using HBase directly in a local environment will more than likely receive a 404.
   */
  private def fromResponseToErrorOrJson(response: WSResponse): Either[ErrorMessage, Option[JsValue]] = {
    logger.info(s"HBase response has status ${describeStatus(response)}")
    response.status match {
      case OK => bodyAsJson(response)
      case NOT_FOUND => Right(None)
      case UNAUTHORIZED => Left(describeStatus(response) + " - check HBase REST configuration")
      case _ => Left(describeStatus(response))
    }
  }

  private def bodyAsJson(response: WSResponse): Either[ErrorMessage, Option[JsValue]] =
    TrySupport.fold(Try(response.json))(
      err => Left(s"Unable to create JsValue from HBase response [${err.getMessage}]"),
      json => Right(json)
    ).right.map(Some(_))

  private def describeStatus(response: WSResponse): String =
    s"${response.statusText} (${response.status})"

  private def convertToErrorOrRows(withReader: Reads[Seq[Row]])(errorOrJson: Either[ErrorMessage, Option[JsValue]]): Either[ErrorMessage, Seq[Row]] =
    errorOrJson.right.flatMap { optJson =>
      logger.debug(s"HBase REST response JSON is [$optJson]")
      optJson.fold[Either[ErrorMessage, Seq[Row]]](Right(Seq.empty)) { json =>
        parseJson(withReader)(json)
      }
    }

  private def parseJson(readsRows: Reads[Seq[Row]])(json: JsValue): Either[ErrorMessage, Seq[Row]] = {
    val eitherErrorOrRows = readsRows.reads(json).asEither
    logger.debug(s"HBase REST parsed response is [$eitherErrorOrRows]")
    eitherErrorOrRows.left.map(errors => s"Unable to parse HBase REST json response [$errors].")
  }

  private def verifyAtMostOneRow(errorOrRows: Either[ErrorMessage, Seq[Row]]): Either[ErrorMessage, Option[Row]] =
    errorOrRows.right.flatMap { rows =>
      if (rows.size > 1) {
        logger.warn(s"At most one result was expected for query but found [$rows].")
        Left(s"At most one result was expected but found [${rows.size}]")
      } else Right(rows.headOption)
    }

  private def withTranslationOfFailureToError[B] = new PartialFunction[Throwable, Either[ErrorMessage, B]] {
    override def isDefinedAt(cause: Throwable): Boolean = true

    override def apply(cause: Throwable): Either[ErrorMessage, B] = {
      logger.info(s"Translating HBase request failure [$cause].")
      cause match {
        case t: TimeoutException => Left(s"Timeout.  ${t.getMessage}")
        case t: Throwable => Left(t.getMessage)
      }
    }
  }

  /*
   * A HBase checkAndUpdate operation returns 304: Not Modified when:
   * - the "check" is not satisfied (ie. the value has been changed by another user)
   * - there is no such row with the target rowKey
   *
   * In order to provide a RESTful interface and distinguish Conflict from Not Found we therefore perform a GET first.
   * In order to do a GET, we need to determine the relevant column family.
   */
  override def update(table: String, rowKey: RowKey, checkField: Field, updateField: Field): Future[UpdateResult] =
    columnFamilyOf(checkField).fold[Future[UpdateResult]](Future.successful(UpdateRejected)) { columnFamily =>
      findRow(table, rowKey, columnFamily).flatMap {
        _.fold[Future[UpdateResult]](
          _ => Future.successful(UpdateFailed),
          _.fold[Future[UpdateResult]](Future.successful(UpdateTargetNotFound)) { _ =>
            checkAndUpdate(table, rowKey, checkField, updateField)
          }
        )
      }
    }

  private def columnFamilyOf(field: Field): Option[String] =
    Column.unapply(field._1).map(_._1)

  private def checkAndUpdate(table: String, rowKey: RowKey, checkField: Field, updateField: Field): Future[UpdateResult] = {
    val url = HBase.checkedPutUrl(withBase = config.baseUrl, config.namespace, table, rowKey)
    logger.info(s"Requesting update of [$url] via HBase REST.")
    val checkAndUpdateJson = toJson(CheckAndUpdate(rowKey, checkField, updateField))(HBaseData.format)
    baseRequest(url).withHeaders(CONTENT_TYPE -> JSON).put(checkAndUpdateJson).map {
      toUpdateResult
    }.recover {
      case _: Throwable => UpdateFailed
    }
  }

  private def toUpdateResult(response: WSResponse): UpdateResult =
    response.status match {
      case OK => UpdateApplied
      case NOT_MODIFIED => UpdateConflicted
      case _ => UpdateFailed
    }
}