package repository.hbase.reportingunit

import com.typesafe.scalalogging.LazyLogging
import org.slf4j.Logger
import repository.Field.{ mandatoryBigDecimalNamed, mandatoryIntNamed, mandatoryStringNamed, optionalStringNamed }
import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.reportingunit.ReportingUnitColumns._
import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnit, Rurn }

import scala.util.Try

/*
 * The following fields are optional:
 * - ruref
 * - entref
 * - tradingStyle
 * - address2
 * - address3
 * - address4
 * - address5
 *
 * Note that we use '=' for these fields within the body of the for expression rather than a generator '<-', so
 * that we capture the field as an Option.
 */
object ReportingUnitRowMapper extends RowMapper[ReportingUnit] with LazyLogging {

  private case class HeadCount(employees: Int, employment: Int)

  private implicit val fieldLogger: Logger = logger.underlying

  override def fromRow(variables: Row): Option[ReportingUnit] = {
    val fields = variables.fields
    for {
      rurn <- mandatoryStringNamed(rurn).apply(fields)
      optRuref = optionalStringNamed(ruref).apply(fields)
      enterpriseLink <- toEnterpriseLink(fields)
      name <- mandatoryStringNamed(name).apply(fields)
      optTradingStyle = optionalStringNamed(tradingStyle).apply(fields)
      legalStatus <- mandatoryStringNamed(legalStatus).apply(fields)
      address <- toAddress(fields)
      sic07 <- mandatoryStringNamed(sic07).apply(fields)
      headCount <- tryToHeadCount(fields).toOption
      turnover <- mandatoryIntNamed(turnover).apply(fields).toOption
      prn <- mandatoryBigDecimalNamed(prn).apply(fields).toOption
      region <- mandatoryStringNamed(region).apply(fields)
    } yield ReportingUnit(Rurn(rurn), optRuref, enterpriseLink, name, optTradingStyle, legalStatus, address,
      sic07, headCount.employees, headCount.employment, turnover, prn, region)
  }

  private def toEnterpriseLink(fields: Map[String, String]): Option[EnterpriseLink] =
    for {
      ern <- mandatoryStringNamed(ern).apply(fields)
      optEntref = optionalStringNamed(entref).apply(fields)
    } yield EnterpriseLink(Ern(ern), optEntref)

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
