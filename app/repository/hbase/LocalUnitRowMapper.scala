package repository.hbase

import repository.RowMapper
import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.localunit.{ Address, LocalUnit, Lurn }

import scala.util.Try
import LocalUnitColumns._

object LocalUnitRowMapper extends RowMapper[LocalUnit] {

  override def fromRow(variables: Map[String, String]): Option[LocalUnit] =
    for {
      lurn <- variables.get(lurn)
      luref <- variables.get(luref)
      name <- variables.get(name)
      tradingStyle <- variables.get(tradingstyle)
      sic07 <- variables.get(sic07)
      employees <- variables.get(employees)
      employeesInt <- Try(employees.toInt).toOption
      enterpriseLink <- toEnterpriseLink(variables)
      address <- toAddress(variables)
    } yield LocalUnit(Lurn(lurn), luref, name, tradingStyle, sic07, employeesInt, enterpriseLink, address)

  private def toEnterpriseLink(variables: Map[String, String]): Option[EnterpriseLink] =
    for {
      ern <- variables.get(ern)
      entref <- variables.get(entref)
    } yield EnterpriseLink(Ern(ern), entref)

  private def toAddress(variables: Map[String, String]): Option[Address] =
    for {
      line1 <- variables.get(address1)
      line2 <- variables.get(address2)
      line3 <- variables.get(address3)
      line4 <- variables.get(address4)
      line5 <- variables.get(address5)
      postcode <- variables.get(postcode)
    } yield Address(line1, line2, line3, line4, line5, postcode)
}