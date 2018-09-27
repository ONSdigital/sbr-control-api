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
import repository.RestRepository._
import repository._
import repository.hbase.HBaseData.HBaseRow
import repository.hbase.HBaseRestRepository._
import utils.{ BaseUrl, ResponseHandler, TrySupport }

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
    val url = HBase.rowKeyColFamilyUrl(withBase = config.baseUrl, config.namespace, table, query, columnFamily)
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

  override def createOrReplace(table: String, rowKey: RowKey, field: Field): Future[CreateOrReplaceResult] = {
    val url = HBase.rowKeyUrl(withBase = config.baseUrl, config.namespace, table, rowKey)
    logger.info(s"Requesting creation/replacement of [${field._1}] at [$url] via HBase REST.")
    asCreateOrReplaceResult(putEdit(url, SingleField(rowKey, field)))
  }

  override def updateField(table: String, rowKey: RowKey, checkField: Field, updateField: Field): Future[OptimisticEditResult] =
    ifExists(table, rowKey, columnOf(checkField).family) { _ =>
      val updateUrl = HBase.checkedPutUrl(withBase = config.baseUrl, config.namespace, table, rowKey)
      logger.info(s"Requesting update of [${updateField._1}] at [$updateUrl] via HBase REST.")
      doCheckedEdit(updateUrl, CheckAndUpdate(rowKey, checkField, updateField))
    }

  /*
   * Note that if the target column does not exist, we take no action and report a successful deletion.
   * This is the correct behaviour for an idempotent delete operation, and ensures that the operation can be retried
   * in the event of any failure.
   * However, it was probably a mistake to handle this here.  The client request is actually a patch containing
   * test & remove operations.  If the test condition does not hold, the patch operation should be aborted.  In
   * focusing on the delete operation we have failed to correctly adhere to the expected semantics of patch.
   */
  override def deleteField(table: String, rowKey: RowKey, checkField: Field, columnName: Column): Future[OptimisticEditResult] =
    ifExists(table, rowKey, columnName.family) { row =>
      if (row.fields.contains(columnName.qualifier)) checkAndDelete(table, rowKey, checkField, columnName)
      else Future.successful(EditApplied)
    }

  private def checkAndDelete(table: String, rowKey: RowKey, checkField: Field, columnName: Column): Future[OptimisticEditResult] = {
    val deleteUrl = HBase.checkedDeleteUrl(withBase = config.baseUrl, config.namespace, table, rowKey, columnName)
    logger.info(s"Requesting delete at [$deleteUrl] via HBase REST")
    doCheckedEdit(deleteUrl, SingleField(rowKey, checkField))
  }

  private def doCheckedEdit(url: String, body: Seq[HBaseRow]): Future[OptimisticEditResult] =
    asOptimisticEditResult(putEdit(url, body))

  private def putEdit(url: String, body: Seq[HBaseRow]): Future[WSResponse] =
    putJson(url, toJson(body)(HBaseData.format))

  private def putJson(url: String, body: JsValue): Future[WSResponse] =
    baseRequest(url).withHeaders(CONTENT_TYPE -> JSON).put(body)

  /*
   * A HBase "checked action" (either an update or delete) simply returns 304: Not Modified when no action is taken.
   * This can be because the "check" is not satisfied (i.e. the value has been changed by another user), but may
   * simply be because there is no such row with the target key.
   *
   * In order for us to provide a proper RESTful interface for our clients, we need to distinguish between the
   * Conflict and Not Found cases.  In order to do this, we perform a GET operation first.
   */
  private def ifExists(table: String, rowKey: RowKey, columnFamily: String)(edit: Row => Future[OptimisticEditResult]): Future[OptimisticEditResult] =
    findRow(table, rowKey, columnFamily).flatMap {
      _.fold(
        _ => Future.successful(EditFailed),
        _.fold[Future[OptimisticEditResult]](Future.successful(EditTargetNotFound))(edit(_))
      )
    }
}

private object HBaseRestRepository {
  def columnOf(field: Field): Column =
    field._1

  val asOptimisticEditResult: Future[WSResponse] => Future[OptimisticEditResult] =
    ResponseHandler.make(toOptimisticEditResult) {
      case _: Throwable => EditFailed
    }

  private def toOptimisticEditResult(response: WSResponse): OptimisticEditResult =
    response.status match {
      case OK => EditApplied
      case NOT_MODIFIED => EditConflicted
      case _ => EditFailed
    }

  val asCreateOrReplaceResult: Future[WSResponse] => Future[CreateOrReplaceResult] =
    ResponseHandler.make(toCreateOrReplaceResult) {
      case _: Throwable => EditFailed
    }

  private def toCreateOrReplaceResult(response: WSResponse): CreateOrReplaceResult =
    response.status match {
      case OK => EditApplied
      case _ => EditFailed
    }
}