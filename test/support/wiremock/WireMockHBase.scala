package support.wiremock

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.{ equalTo, get, put, urlEqualTo }
import com.typesafe.scalalogging.LazyLogging
import play.api.http.HeaderNames.{ ACCEPT, CONTENT_TYPE }
import play.mvc.Http.MimeTypes.JSON
import repository.hbase.HBase._
import repository.hbase.{ Column, PeriodTableName }
import repository.hbase.enterprise.EnterpriseUnitRowKey
import repository.hbase.legalunit.LegalUnitQuery
import repository.hbase.localunit.LocalUnitQuery
import repository.hbase.reportingunit.ReportingUnitQuery
import repository.hbase.unitlinks.{ HBaseRestUnitLinksRepository, UnitLinksQualifier, UnitLinksRowKey }
import support.{ BasicAuthentication, HBaseJsonBodyFixture }
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.Ubrn
import uk.gov.ons.sbr.models.localunit.Lurn
import uk.gov.ons.sbr.models.reportingunit.Rurn
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitType }

trait WireMockHBase extends ApiResponse with BasicAuthentication with HBaseJsonBodyFixture with LazyLogging {
  /*
   * Note that the default value in application.conf is overridden to this value for tests (in build.sbt)
   */
  val DefaultHBasePort = 8075
  private var wireMockSupportContainer: Option[WireMockSupport] = None

  def startMockHBase(port: Int = DefaultHBasePort): Unit = {
    logger.debug(s"Starting WireMockHBase on port [$port].")
    wireMockSupportContainer = Some(WireMockSupport.start(port))
  }

  def stopMockHBase(): Unit = {
    wireMockSupportContainer.foreach { wm =>
      logger.debug(s"Stopping WireMockHBase on port [${WireMockSupport.port(wm)}].")
      WireMockSupport.stop(wm)
    }
    wireMockSupportContainer = None
  }

  def withWireMockHBase[A](fn: () => A): A = {
    startMockHBase()
    try fn()
    finally stopMockHBase()
  }

  def stubHBaseFor(mappingBuilder: MappingBuilder): Unit = {
    require(wireMockSupportContainer.isDefined, "WireMockHBase must be started before it can be stubbed")
    wireMockSupportContainer.foreach(wm => WireMockSupport.registerMapping(wm)(mappingBuilder))
  }

  private val Namespace = "sbr_control_db"

  def anAllLocalUnitsForEnterpriseRequest(withErn: Ern, withPeriod: Period): MappingBuilder =
    aLocalUnitQuery(withPeriod, LocalUnitQuery.forAllWith(withErn))

  def aLocalUnitRequest(withErn: Ern, withPeriod: Period, withLurn: Lurn): MappingBuilder =
    aLocalUnitQuery(withPeriod, LocalUnitQuery.byRowKey(withErn, withLurn))

  private def aLocalUnitQuery(withPeriod: Period, withQuery: String): MappingBuilder = {
    val tableName = PeriodTableName("local_unit", withPeriod)
    getHBaseJson(url = urlFor(tableName, withQuery, DefaultColumnFamily), auth = dummyAuthorization)
  }

  def anAllReportingUnitsForEnterpriseRequest(withErn: Ern, withPeriod: Period): MappingBuilder =
    aReportingUnitQuery(withPeriod, ReportingUnitQuery.forAllWith(withErn))

  def aReportingUnitRequest(withErn: Ern, withPeriod: Period, withRurn: Rurn): MappingBuilder =
    aReportingUnitQuery(withPeriod, ReportingUnitQuery.byRowKey(withErn, withRurn))

  private def aReportingUnitQuery(withPeriod: Period, withQuery: String): MappingBuilder = {
    val tableName = PeriodTableName("reporting_unit", withPeriod)
    getHBaseJson(url = urlFor(tableName, withQuery, DefaultColumnFamily), auth = dummyAuthorization)
  }

  def anAllLegalUnitsForEnterpriseRequest(withErn: Ern, withPeriod: Period): MappingBuilder =
    aLegalUnitQuery(withPeriod, LegalUnitQuery.forAllWith(withErn))

  def aLegalUnitRequest(withErn: Ern, withPeriod: Period, withUbrn: Ubrn): MappingBuilder =
    aLegalUnitQuery(withPeriod, LegalUnitQuery.byRowKey(withErn, withUbrn))

  private def aLegalUnitQuery(withPeriod: Period, withQuery: String): MappingBuilder = {
    val tableName = PeriodTableName("legal_unit", withPeriod)
    getHBaseJson(url = urlFor(tableName, withQuery, DefaultColumnFamily), auth = dummyAuthorization)
  }

  def anEnterpriseUnitRequest(withErn: Ern, withPeriod: Period): MappingBuilder = {
    val tableName = PeriodTableName("enterprise", withPeriod)
    getHBaseJson(url = urlFor(tableName, EnterpriseUnitRowKey(withErn), DefaultColumnFamily), auth = dummyAuthorization)
  }

  def aUnitLinksExactRowKeyRequest(withUnitId: UnitId, withUnitType: UnitType, withPeriod: Period): MappingBuilder = {
    val url = aUnitLinksUrlBuilder(urlFor(_, _, HBaseRestUnitLinksRepository.ColumnFamily))(withUnitType, withUnitId, withPeriod)
    getHBaseJson(url, auth = dummyAuthorization)
  }

  def aCheckAndUpdateUnitLinkRequest(withUnitType: UnitType, withUnitId: UnitId, withPeriod: Period): MappingBuilder = {
    val url = aUnitLinksUrlBuilder(urlForCheckedPut)(withUnitType, withUnitId, withPeriod)
    putHBaseJson(url, auth = dummyAuthorization)
  }

  def aCreateUnitLinkRequest(withUnitType: UnitType, withUnitId: UnitId, withPeriod: Period): MappingBuilder = {
    val url = aUnitLinksUrlBuilder(urlForUncheckedPut)(withUnitType, withUnitId, withPeriod)
    putHBaseJson(url, auth = dummyAuthorization)
  }

  def aCheckAndDeleteChildUnitLinkRequest(withUnitType: UnitType, withUnitId: UnitId, withPeriod: Period, withChildId: UnitId): MappingBuilder = {
    val columnName = HBaseRestUnitLinksRepository.columnNameFor(UnitLinksQualifier.toChild(withChildId))
    val url = aUnitLinksUrlBuilder(urlForCheckedDelete(_, _, columnName))(withUnitType, withUnitId, withPeriod)
    putHBaseJson(url, auth = dummyAuthorization)
  }

  def getHBaseJson(url: String, auth: Authorization): MappingBuilder =
    get(urlEqualTo(url)).
      withHeader(ACCEPT, equalTo(JSON)).
      withHeader(Authorization.Name, equalTo(Authorization.value(auth)))

  def putHBaseJson(url: String, auth: Authorization): MappingBuilder =
    put(urlEqualTo(url)).
      withHeader(CONTENT_TYPE, equalTo(JSON)).
      withHeader(Authorization.Name, equalTo(Authorization.value(auth)))

  private def aUnitLinksUrlBuilder(urlBuilder: (String, String) => String)(withUnitType: UnitType, withUnitId: UnitId, withPeriod: Period): String = {
    val tableName = PeriodTableName("unit_link", withPeriod)
    val rowKey = UnitLinksRowKey(withUnitId, withUnitType)
    urlBuilder(tableName, rowKey)
  }

  private def urlFor(tableName: String, rowKey: String, columnFamily: String): String =
    "/" + rowKeyColFamilyUrl(namespace = Namespace, tableName, rowKey, columnFamily)

  private def urlForCheckedPut(tableName: String, rowKey: String): String =
    "/" + checkedPutUrl(namespace = Namespace, tableName, rowKey)

  private def urlForUncheckedPut(tableName: String, rowKey: String): String =
    "/" + rowKeyUrl(namespace = Namespace, tableName, rowKey)

  private def urlForCheckedDelete(tableName: String, rowKey: String, columnName: Column): String =
    "/" + checkedDeleteUrl(namespace = Namespace, tableName, rowKey, columnName)

  private val dummyAuthorization: Authorization =
    Authorization("", "")
}
