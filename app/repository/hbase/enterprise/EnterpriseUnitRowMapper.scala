package repository.hbase.enterprise

import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.enterprise.EnterpriseUnitColumns._
import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern, Turnover }

import scala.util.{ Success, Try }

object EnterpriseUnitRowMapper extends RowMapper[Enterprise] {

  override def fromRow(variables: Row): Option[Enterprise] =
    for {
      ern <- variables.fields.get(ern)
      entrefOpt = variables.fields.get(entref)
      name <- variables.fields.get(name)
      tradingStyleOpt = variables.fields.get(tradingStyle)
      address <- toAddress(variables.fields)
      legalStatus <- variables.fields.get(legalStatus)
      sic07 <- variables.fields.get(sic07)
      (employeesOpt, jobsOpt) <- tryToEmployeesJobs(variables.fields).toOption
      turnoverOpt <- tryToTurnover(variables.fields).toOption
    } yield Enterprise(Ern(ern), entrefOpt, name, tradingStyleOpt, address, sic07, legalStatus,
      employeesOpt, jobsOpt, turnoverOpt)

  private def toAddress(fields: Map[String, String]): Option[Address] =
    for {
      line1 <- fields.get(address1)
      optLine2 = fields.get(address2)
      optLine3 = fields.get(address3)
      optLine4 = fields.get(address4)
      optLine5 = fields.get(address5)
      postcode <- fields.get(postcode)
    } yield Address(line1, optLine2, optLine3, optLine4, optLine5, postcode)

  private def tryToEmployeesJobs(fields: Map[String, String]): Try[(Option[Int], Option[Int])] =
    for {
      employeesOpt <- tryToOptionalInt(fields.get(employees))
      jobsOpt <- tryToOptionalInt(fields.get(jobs))
    } yield (employeesOpt, jobsOpt)

  private def tryToTurnover(fields: Map[String, String]): Try[Option[Turnover]] =
    for {
      containedTurnoverOpt <- tryToOptionalInt(fields.get(containedTurnover))
      standardTurnoverOpt <- tryToOptionalInt(fields.get(standardTurnover))
      groupTurnoverOpt <- tryToOptionalInt(fields.get(groupTurnover))
      apportionedTurnoverOpt <- tryToOptionalInt(fields.get(apportionedTurnover))
      enterpriseTurnoverOpt <- tryToOptionalInt(fields.get(enterpriseTurnover))
      // a single option containing the first Some; else None
      anyTurnoverOpt = containedTurnoverOpt.orElse(standardTurnoverOpt).orElse(groupTurnoverOpt).orElse(
        apportionedTurnoverOpt
      ).orElse(enterpriseTurnoverOpt)
    } yield anyTurnoverOpt.map { _ =>
      Turnover(containedTurnoverOpt, standardTurnoverOpt, groupTurnoverOpt, apportionedTurnoverOpt, enterpriseTurnoverOpt)
    }

  private def tryToOptionalInt(strOpt: Option[String]): Try[Option[Int]] =
    strOpt.fold[Try[Option[Int]]](Success(None)) { str =>
      tryToInt(str).map(Some(_))
    }

  private def tryToInt(str: String): Try[Int] =
    Try(str.toInt)
}
