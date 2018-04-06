package repository.hbase

import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import play.api.http.MimeTypes.JSON
import play.api.http.Status.NOT_FOUND
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Reads
import play.api.libs.ws.WSAuthScheme.BASIC
import play.api.libs.ws.{ WSClient, WSResponse }
import repository.RestRepository
import repository.RestRepository.Row

import scala.concurrent.Future
import scala.concurrent.duration._

case class HBaseRestRepositoryConfig(protocolWithHostname: String, port: String,
  namespace: String, username: String, password: String, timeout: Long)

class HBaseRestRepository @Inject() (
  config: HBaseRestRepositoryConfig,
    wsClient: WSClient,
    responseReaderMaker: HBaseResponseReaderMaker
) extends RestRepository with LazyLogging {
  private type ResponseReader = WSResponse => Seq[Row]

  override def get(table: String, rowKey: String, columnGroup: String): Future[Seq[Row]] = {
    val url = HBase.rowKeyUrl(config.protocolWithHostname, config.port, config.namespace, table, rowKey, columnGroup)
    val responseReader = readResponseBody(responseReaderMaker.forColumnGroup(columnGroup)) _
    logger.info(s"Requesting [$url] from HBase REST.")
    wsClient.url(url).
      withHeaders("Accept" -> JSON).
      withAuth(config.username, config.password, scheme = BASIC).
      withRequestTimeout(config.timeout.milliseconds).
      get().
      map(processResponse(responseReader))
  }

  /*
   * Note that official environments running Cloudera will receive an OK result containing an "empty row" on Not Found.
   * Developers using HBase directly in a local environment will more than likely receive a 404.
   */
  private def processResponse(read: ResponseReader)(response: WSResponse): Seq[Row] =
    if (response.status == NOT_FOUND) {
      logger.info("HBase REST result was NOT_FOUND.  Returning empty sequence.")
      Seq.empty
    } else read(response)

  private def readResponseBody(readsRows: Reads[Seq[Row]])(response: WSResponse): Seq[Row] = {
    val jsResult = readsRows.reads(response.json)
    logger.debug(s"HBase REST parse result was [$jsResult].")
    jsResult.getOrElse(throw new AssertionError("Unable to parse HBase REST JSON result"))
  }
}
