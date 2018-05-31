package repository.hbase.legalunit

import scala.util.Try
import utils.TrySupport

import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.legalunit.{ LegalUnit, UBRN }
import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.legalunit.LegalUnitColumns._
import uk.gov.ons.sbr.models.Address

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
object LegalUnitRowMapper extends RowMapper[LegalUnit] {

  override def fromRow(variables: Row): Option[LegalUnit] =
    for {
      ubrn <- variables.fields.get(ubrn)
      name <- variables.fields.get(name)
      sic07 <- variables.fields.get(sic07)
      legalStatus <- variables.fields.get(legalStatus)
      tradingStatus <- variables.fields.get(tradingStatus)
      enterpriseLink <- toEnterpriseLink(variables)
      address <- toAddress(variables)
      optTradingStyle = variables.fields.get(tradingstyle)
      optCrn = variables.fields.get(crn)

      jobsStr = variables.fields.get(jobs)
      jobsOptTry = asInt(jobsStr)
      if invalidInt(jobsOptTry)
      jobsOptInt = parseTry(jobsOptTry)

      turnoverStr = variables.fields.get(turnover)
      turnoverOptTry = asInt(turnoverStr)
      if invalidInt(turnoverOptTry)
      turnoverOptInt = parseTry(turnoverOptTry)

    } yield LegalUnit(UBRN(ubrn), optCrn, name, legalStatus, tradingStatus, optTradingStyle, sic07, turnoverOptInt, jobsOptInt, enterpriseLink, address)

  private def toEnterpriseLink(variables: Row): Option[EnterpriseLink] =
    for {
      ern <- variables.fields.get(ern)
      optEntref = variables.fields.get(entref)
    } yield EnterpriseLink(Ern(ern), optEntref)

  private def toAddress(variables: Row): Option[Address] =
    for {
      line1 <- variables.fields.get(address1)
      optLine2 = variables.fields.get(address2)
      optLine3 = variables.fields.get(address3)
      optLine4 = variables.fields.get(address4)
      optLine5 = variables.fields.get(address5)
      postcode <- variables.fields.get(postcode)
    } yield Address(line1, optLine2, optLine3, optLine4, optLine5, postcode)

  private def parseTry(valueOptTry: Option[Try[Int]]) =
    valueOptTry.fold[Option[Int]](None) { tryToInt =>
      // TODO - Add logger for Assertion Exception
      TrySupport.fold(tryToInt)(failure => throw failure, integralVal => Some(integralVal))
    }

  private def asInt(fieldAsStr: Option[String]): Option[Try[Int]] =
    fieldAsStr.map(x => Try(x.toInt))

  private def invalidInt(fieldOptTry: Option[Try[Int]]) =
    fieldOptTry.fold(true)(_.isSuccess)
}
