package repository.hbase.enterprise

import com.typesafe.scalalogging.LazyLogging
import org.slf4j.Logger
import repository.Field._
import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.enterprise.EnterpriseUnitColumns._
import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern, Turnover }

import scala.util.Try

object EnterpriseUnitRowMapper extends RowMapper[Enterprise] with LazyLogging {

  private case class HeadCount(
    employeesOpt: Option[Int],
    jobsOpt: Option[Int],
    workingProprietors: Int,
    employment: Int
  )

  private implicit val fieldLogger: Logger = logger.underlying

  override def fromRow(variables: Row): Option[Enterprise] = {
    val fields = variables.fields
    for {
      ern <- mandatoryStringNamed(ern).apply(fields)
      entrefOpt = optionalStringNamed(entref).apply(fields)
      name <- mandatoryStringNamed(name).apply(fields)
      tradingStyleOpt = optionalStringNamed(tradingStyle).apply(fields)
      address <- toAddress(fields)
      legalStatus <- mandatoryStringNamed(legalStatus).apply(fields)
      sic07 <- mandatoryStringNamed(sic07).apply(fields)
      headCount <- tryToHeadCount(fields).toOption
      turnoverOpt <- tryToTurnover(fields).toOption
      prn <- mandatoryBigDecimalNamed(prn).apply(fields).toOption
      region <- mandatoryStringNamed(region).apply(fields)
    } yield Enterprise(Ern(ern), entrefOpt, name, tradingStyleOpt, address, sic07, legalStatus, headCount.employeesOpt,
      headCount.jobsOpt, turnoverOpt, prn, headCount.workingProprietors, headCount.employment, region)
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

  private def tryToHeadCount(fields: Map[String, String]): Try[HeadCount] =
    for {
      employeesOpt <- optionalIntNamed(employees).apply(fields)
      jobsOpt <- optionalIntNamed(jobs).apply(fields)
      workingProprietors <- mandatoryIntNamed(workingProprietors).apply(fields)
      employment <- mandatoryIntNamed(employment).apply(fields)
    } yield HeadCount(employeesOpt, jobsOpt, workingProprietors, employment)

  private def tryToTurnover(fields: Map[String, String]): Try[Option[Turnover]] =
    for {
      containedTurnoverOpt <- optionalIntNamed(containedTurnover).apply(fields)
      standardTurnoverOpt <- optionalIntNamed(standardTurnover).apply(fields)
      groupTurnoverOpt <- optionalIntNamed(groupTurnover).apply(fields)
      apportionedTurnoverOpt <- optionalIntNamed(apportionedTurnover).apply(fields)
      enterpriseTurnoverOpt <- optionalIntNamed(enterpriseTurnover).apply(fields)
      // a single option containing the first Some; else None
      anyTurnoverOpt = containedTurnoverOpt.orElse(standardTurnoverOpt).orElse(groupTurnoverOpt).orElse(
        apportionedTurnoverOpt
      ).orElse(enterpriseTurnoverOpt)
    } yield anyTurnoverOpt.map { _ =>
      Turnover(containedTurnoverOpt, standardTurnoverOpt, groupTurnoverOpt, apportionedTurnoverOpt, enterpriseTurnoverOpt)
    }
}
