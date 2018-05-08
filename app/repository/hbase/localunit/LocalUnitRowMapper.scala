package repository.hbase.localunit

import scala.util.Try

import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.localunit.{ Address, LocalUnit, Lurn }

import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.localunit.LocalUnitColumns._

/*
 * The following fields are optional:
 * - luref
 * - entref
 * - tradingstyle
 * - address2
 * - address3
 * - address4
 * - address5
 *
 * Note that we use '=' for these fields within the body of the for expression rather than a generator '<-', so
 * that we capture the field as an Option.
 */
object LocalUnitRowMapper extends RowMapper[LocalUnit] {

  override def fromRow(variables: Row): Option[LocalUnit] =
    for {
      lurn <- variables.fields.get(lurn)
      optLuref = variables.fields.get(luref)
      name <- variables.fields.get(name)
      optTradingStyle = variables.fields.get(tradingstyle)
      sic07 <- variables.fields.get(sic07)
      employees <- variables.fields.get(employees)
      employeesInt <- Try(employees.toInt).toOption
      enterpriseLink <- toEnterpriseLink(variables)
      address <- toAddress(variables)
    } yield LocalUnit(Lurn(lurn), optLuref, name, optTradingStyle, sic07, employeesInt, enterpriseLink, address)

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
}
