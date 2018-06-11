package repository.hbase.enterprise

import scala.util.Try

import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern, Turnover }

import utils.TrySupport
import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.enterprise.EnterpriseUnitColumns._

object EnterpriseUnitRowMapper extends RowMapper[Enterprise] {

  override def fromRow(variables: Row): Option[Enterprise] =
    for {
      ern <- variables.fields.get(ern)
      entrefOptStr = variables.fields.get(entref)
      name <- variables.fields.get(name)
      tradingStyleOptStr = variables.fields.get(tradingStyle)
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

      turnoverObjectTry = toTurnover(variables)
      if turnoverObjectTry.isSuccess
      turnoverOpt = turnoverObjectTry.toOption.flatten

    } yield Enterprise(Ern(ern), entrefOptStr, name, tradingStyleOptStr, address, sic07, legalStatus, employeeOptInt,
      jobsOptInt, turnover = turnoverOpt)

  private def toAddress(variables: Row): Option[Address] =
    for {
      line1 <- variables.fields.get(address1)
      optLine2 = variables.fields.get(address2)
      optLine3 = variables.fields.get(address3)
      optLine4 = variables.fields.get(address4)
      optLine5 = variables.fields.get(address5)
      postcode <- variables.fields.get(postcode)
    } yield Address(line1, optLine2, optLine3, optLine4, optLine5, postcode)

  private def toTurnover(variables: Row): Try[Option[Turnover]] = {
    val toIntTakesTurnoverType = optStringToTurnoverInt(variables, _: String)
    Try {
      val containedTurnoverOpt = toIntTakesTurnoverType(containedTurnover)
      val standardTurnoverOpt = toIntTakesTurnoverType(standardTurnover)
      val groupTurnoverOpt = toIntTakesTurnoverType(groupTurnover)
      val apportionedTurnoverOpt = toIntTakesTurnoverType(apportionedTurnover)
      val enterpriseTurnoverOpt = toIntTakesTurnoverType(enterpriseTurnover)
      if (List(containedTurnoverOpt, standardTurnoverOpt, groupTurnoverOpt, apportionedTurnoverOpt, enterpriseTurnoverOpt).forall(_.isEmpty)) None
      else Some(Turnover(containedTurnoverOpt, standardTurnoverOpt, groupTurnoverOpt, apportionedTurnoverOpt, enterpriseTurnoverOpt))
    }
  }

  private def optStringToTurnoverInt(variables: Row, turnoverType: String) = {
    val optStrTurnover = variables.fields.get(turnoverType)
    val optIntOrFail = optStrTurnover.map(_.toInt)
    optIntOrFail
  }

  private def parseTry(valueOptTry: Option[Try[Int]]): Option[Int] =
    valueOptTry.fold[Option[Int]](None) { tryToInt =>
      TrySupport.fold(tryToInt)(_ => None, integralVal => Some(integralVal))
    }

  private def asInt(fieldAsStr: Option[String]): Option[Try[Int]] =
    fieldAsStr.map(x => Try(x.toInt))

  private def invalidInt(fieldOptTry: Option[Try[Int]]) =
    fieldOptTry.fold(true)(_.isSuccess)

}
