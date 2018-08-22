package repository.hbase.legalunit

import com.typesafe.scalalogging.LazyLogging
import org.slf4j.Logger
import repository.Field.{ mandatoryStringNamed, optionalIntNamed, optionalStringNamed }
import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.legalunit.LegalUnitColumns._
import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.legalunit.{ Crn, LegalUnit, Ubrn, Uprn }

/*
 * The following fields are optional:
 * - tradingStyle
 * - address2
 * - address3
 * - address4
 * - address5
 * - payeJobs
 * - turnover
 * - tradingStatus
 * - deathDate
 * - deathCode
 * - crn
 * - uprn
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
      optTradingStyle = optionalStringNamed(tradingStyle).apply(fields)
      address <- toAddress(fields)
      sic07 <- mandatoryStringNamed(sic07).apply(fields)
      optPayeJobs <- optionalIntNamed(payeJobs).apply(fields).toOption
      optTurnover <- optionalIntNamed(turnover).apply(fields).toOption
      legalStatus <- mandatoryStringNamed(legalStatus).apply(fields)
      optTradingStatus = optionalStringNamed(tradingStatus).apply(fields)
      birthDate <- mandatoryStringNamed(birthDate).apply(fields)
      optDeathDate = optionalStringNamed(deathDate).apply(fields)
      optDeathCode = optionalStringNamed(deathCode).apply(fields)
      optCrn = optionalStringNamed(crn).apply(fields).map(Crn(_))
      optUprn = optionalStringNamed(uprn).apply(fields).map(Uprn(_))
    } yield LegalUnit(Ubrn(ubrn), name, legalStatus, optTradingStatus, optTradingStyle, sic07, optTurnover,
      optPayeJobs, address, birthDate, optDeathDate, optDeathCode, optCrn, optUprn)
  }

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
