package repository.hbase.reportingunit

import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.reportingunit.ReportingUnitColumns._
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnit, Rurn }

import scala.util.Try

object ReportingUnitRowMapper extends RowMapper[ReportingUnit] {

  override def fromRow(variables: Row): Option[ReportingUnit] =
    for {
      rurn <- variables.fields.get(rurn)
      optRuref = variables.fields.get(ruref)
      ern <- variables.fields.get(ern)
      optEntref = variables.fields.get(entref)
      name <- variables.fields.get(name)
      optTradingStyle = variables.fields.get(tradingStyle)
      optLegalStatus = variables.fields.get(legalStatus)
      address1 <- variables.fields.get(address1)
      optAddress2 = variables.fields.get(address2)
      optAddress3 = variables.fields.get(address3)
      optAddress4 = variables.fields.get(address4)
      optAddress5 = variables.fields.get(address5)
      postcode <- variables.fields.get(postcode)
      sic07 <- variables.fields.get(sic07)
      employees <- variables.fields.get(employees)
      employeesInt <- Try(employees.toInt).toOption
      employment <- variables.fields.get(employment)
      employmentInt <- Try(employment.toInt).toOption
      turnover <- variables.fields.get(turnover)
      turnoverInt <- Try(turnover.toInt).toOption
      prn <- variables.fields.get(prn)
    } yield ReportingUnit(Rurn(rurn), optRuref, Ern(ern), optEntref, name, optTradingStyle, optLegalStatus,
      address1, optAddress2, optAddress3, optAddress4, optAddress5, postcode, sic07, employeesInt, employmentInt,
      turnoverInt, prn)
}
