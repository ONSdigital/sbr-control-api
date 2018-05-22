package repository.hbase.enterprise

import scala.util.Try

import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern }

import utils.TrySupport
import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.enterprise.EnterpriseUnitColumns._

object EnterpriseUnitRowMapper extends RowMapper[Enterprise] {

  override def fromRow(variables: Row): Option[Enterprise] =
    for {
      ern <- variables.fields.get(ern)
      entrefStr = variables.fields.get(entref)
      name <- variables.fields.get(name)
      tradingStyleStr = variables.fields.get(tradingStyle)
      address <- toAddress(variables)
      legalStatus <- variables.fields.get(legalStatus)
      sic07 <- variables.fields.get(sic07)

      jobsStr = variables.fields.get(jobs)
      jobsOptTry = asInt(jobsStr)
      if invalidInt(jobsOptTry)
      jobsOptInt = parseTry(jobsOptTry)

      employeesStr = variables.fields.get(employees)
      employeeOptTry = asInt(employeesStr)
      if invalidInt(employeeOptTry)
      employeeOptInt = parseTry(employeeOptTry)

      containedTurnoverStr = variables.fields.get(containedTurnover)
      containedTurnoverOptTry = asInt(containedTurnoverStr)
      if invalidInt(containedTurnoverOptTry)
      containedTurnoverOptInt = parseTry(containedTurnoverOptTry)

      standardTurnoverStr = variables.fields.get(containedTurnover)
      standardTurnoverOptTry = asInt(standardTurnoverStr)
      if invalidInt(standardTurnoverOptTry)
      standardTurnoverOptInt = parseTry(standardTurnoverOptTry)

      groupTurnoverStr = variables.fields.get(groupTurnover)
      groupTurnoverOptTry = asInt(groupTurnoverStr)
      if invalidInt(groupTurnoverOptTry)
      groupTurnoverOptInt = parseTry(groupTurnoverOptTry)

      apportionedTurnoverStr = variables.fields.get(apportionedTurnover)
      apportionedTurnoverOptTry = asInt(apportionedTurnoverStr)
      if invalidInt(apportionedTurnoverOptTry)
      apportionedTurnoverOptInt = parseTry(apportionedTurnoverOptTry)

      enterpriseTurnoverStr = variables.fields.get(enterpriseTurnover)
      enterpriseTurnoverOptTry = asInt(enterpriseTurnoverStr)
      if invalidInt(enterpriseTurnoverOptTry)
      enterpriseTurnoverOptInt = parseTry(enterpriseTurnoverOptTry)

    } yield Enterprise(Ern(ern), entrefStr, name, tradingStyleStr, address, sic07, legalStatus, employeeOptInt, jobsOptInt,
      containedTurnoverOptInt, standardTurnoverOptInt, groupTurnoverOptInt, apportionedTurnoverOptInt,
      enterpriseTurnoverOptInt)

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
