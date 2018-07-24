package repository.hbase.reportingunit

import com.typesafe.scalalogging.LazyLogging
import org.slf4j.Logger
import repository.Field.{ mandatoryBigDecimalNamed, mandatoryIntNamed, mandatoryStringNamed, optionalStringNamed }
import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.reportingunit.ReportingUnitColumns._
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnit, Rurn }

object ReportingUnitRowMapper extends RowMapper[ReportingUnit] with LazyLogging {

  private implicit val fieldLogger: Logger = logger.underlying

  override def fromRow(variables: Row): Option[ReportingUnit] = {
    val fields = variables.fields
    for {
      rurn <- mandatoryStringNamed(rurn).apply(fields)
      optRuref = optionalStringNamed(ruref).apply(fields)
      ern <- mandatoryStringNamed(ern).apply(fields)
      optEntref = optionalStringNamed(entref).apply(fields)
      name <- mandatoryStringNamed(name).apply(fields)
      optTradingStyle = optionalStringNamed(tradingStyle).apply(fields)
      optLegalStatus = optionalStringNamed(legalStatus).apply(fields)
      address1 <- mandatoryStringNamed(address1).apply(fields)
      optAddress2 = optionalStringNamed(address2).apply(fields)
      optAddress3 = optionalStringNamed(address3).apply(fields)
      optAddress4 = optionalStringNamed(address4).apply(fields)
      optAddress5 = optionalStringNamed(address5).apply(fields)
      postcode <- mandatoryStringNamed(postcode).apply(fields)
      sic07 <- mandatoryStringNamed(sic07).apply(fields)
      employees <- mandatoryIntNamed(employees).apply(fields).toOption
      employment <- mandatoryIntNamed(employment).apply(fields).toOption
      turnover <- mandatoryIntNamed(turnover).apply(fields).toOption
      prn <- mandatoryBigDecimalNamed(prn).apply(fields).toOption
    } yield ReportingUnit(Rurn(rurn), optRuref, Ern(ern), optEntref, name, optTradingStyle, optLegalStatus,
      address1, optAddress2, optAddress3, optAddress4, optAddress5, postcode, sic07, employees, employment,
      turnover, prn)
  }
}
