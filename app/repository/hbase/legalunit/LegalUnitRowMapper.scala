package repository.hbase.legalunit

import com.typesafe.scalalogging.LazyLogging
import org.slf4j.Logger
import repository.Field.{ mandatoryStringNamed, optionalIntNamed, optionalStringNamed }
import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.legalunit.LegalUnitColumns._
import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.legalunit.{ LegalUnit, UBRN }

/*
 * The following fields are optional:
 * - jobs
 * - entref
 * - tradingstyle
 * - address2
 * - address3
 * - address4
 * - address5
 * - turnover
 * - crn
 *
 * Note that we use '=' for these fields within the body of the for expression rather than a generator '<-', so
 * that we capture the field as an Option.
 */
object LegalUnitRowMapper extends RowMapper[LegalUnit] with LazyLogging {

  private implicit val fieldLogger: Logger = logger.underlying

  override def fromRow(variables: Row): Option[LegalUnit] = {
    val fields = variables.fields
    for {
      ubrn <- mandatoryStringNamed(ubrn).apply(fields)
      name <- mandatoryStringNamed(name).apply(fields)
      sic07 <- mandatoryStringNamed(sic07).apply(fields)
      legalStatus <- mandatoryStringNamed(legalStatus).apply(fields)
      tradingStatus <- mandatoryStringNamed(tradingStatus).apply(fields)
      enterpriseLink <- toEnterpriseLink(fields)
      address <- toAddress(fields)
      optTradingStyle = optionalStringNamed(tradingstyle).apply(fields)
      optCrn = optionalStringNamed(crn).apply(fields)
      jobsOpt <- optionalIntNamed(jobs).apply(fields).toOption
      turnoverOpt <- optionalIntNamed(turnover).apply(fields).toOption
    } yield LegalUnit(UBRN(ubrn), optCrn, name, legalStatus, tradingStatus, optTradingStyle, sic07, turnoverOpt, jobsOpt, enterpriseLink, address)
  }

  private def toEnterpriseLink(fields: Map[String, String]): Option[EnterpriseLink] =
    for {
      ern <- mandatoryStringNamed(ern).apply(fields)
      optEntref = optionalStringNamed(entref).apply(fields)
    } yield EnterpriseLink(Ern(ern), optEntref)

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
