package repository.hbase.localunit

import com.typesafe.scalalogging.LazyLogging
import org.slf4j.Logger
import repository.Field.{ mandatoryBigDecimalNamed, mandatoryIntNamed, mandatoryStringNamed, optionalStringNamed }
import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.localunit.LocalUnitColumns._
import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.localunit.{ LocalUnit, Lurn }
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnitLink, Rurn }

import scala.util.Try

/*
 * The following fields are optional:
 * - luref
 * - entref
 * - tradingStyle
 * - ruref
 * - address2
 * - address3
 * - address4
 * - address5
 *
 * Note that we use '=' for these fields within the body of the for expression rather than a generator '<-', so
 * that we capture the field as an Option.
 */
object LocalUnitRowMapper extends RowMapper[LocalUnit] with LazyLogging {

  private case class HeadCount(employees: Int, employment: Int)

  private implicit val fieldLogger: Logger = logger.underlying

  override def fromRow(variables: Row): Option[LocalUnit] = {
    val fields = variables.fields
    for {
      lurn <- mandatoryStringNamed(lurn).apply(fields)
      optLuref = optionalStringNamed(luref).apply(fields)
      name <- mandatoryStringNamed(name).apply(fields)
      optTradingStyle = optionalStringNamed(tradingStyle).apply(fields)
      sic07 <- mandatoryStringNamed(sic07).apply(fields)
      headCount <- tryToHeadCount(fields).toOption
      enterpriseLink <- toEnterpriseLink(fields)
      reportingUnitLink <- toReportingUnitLink(fields)
      address <- toAddress(fields)
      prn <- mandatoryBigDecimalNamed(prn).apply(fields).toOption
      region <- mandatoryStringNamed(region).apply(fields)
    } yield LocalUnit(Lurn(lurn), optLuref, enterpriseLink, reportingUnitLink, name, optTradingStyle, address, sic07,
      headCount.employees, headCount.employment, prn, region)
  }

  private def toEnterpriseLink(fields: Map[String, String]): Option[EnterpriseLink] =
    for {
      ern <- mandatoryStringNamed(ern).apply(fields)
      optEntref = optionalStringNamed(entref).apply(fields)
    } yield EnterpriseLink(Ern(ern), optEntref)

  private def toReportingUnitLink(fields: Map[String, String]): Option[ReportingUnitLink] =
    for {
      rurn <- mandatoryStringNamed(rurn).apply(fields)
      optRuref = optionalStringNamed(ruref).apply(fields)
    } yield ReportingUnitLink(Rurn(rurn), optRuref)

  private def tryToHeadCount(fields: Map[String, String]): Try[HeadCount] =
    for {
      employees <- mandatoryIntNamed(employees).apply(fields)
      employment <- mandatoryIntNamed(employment).apply(fields)
    } yield HeadCount(employees, employment)

  private def toAddress(fields: Map[String, String]): Option[Address] =
    for {
      line1 <- mandatoryStringNamed(address1).apply(fields)
      optLine2 = optionalStringNamed(address2).apply(fields)
      optLine3 = optionalStringNamed(address3).apply(fields)
      optLine4 = optionalStringNamed(address4).apply(fields)
      optLine5 = optionalStringNamed(address5).apply(fields)
      postcode <- mandatoryStringNamed(postcode).apply(fields)
    } yield Address(line1, optLine2, optLine3, optLine4, optLine5, postcode)
}
