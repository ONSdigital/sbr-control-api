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
      rurn <- variables.get(rurn)
      optRuref = variables.get(ruref)
      ern <- variables.get(ern)
      optEntref = variables.get(entref)
      name <- variables.get(name)
      optTradingStyle = variables.get(tradingStyle)
      optLegalStatus = variables.get(legalStatus)
      address1 <- variables.get(address1)
      optAddress2 = variables.get(address2)
      optAddress3 = variables.get(address3)
      optAddress4 = variables.get(address4)
      optAddress5 = variables.get(address5)
      postcode <- variables.get(postcode)
      sic07 <- variables.get(sic07)
      employees <- variables.get(employees)
      employeesInt <- Try(employees.toInt).toOption
      employment <- variables.get(employment)
      employmentInt <- Try(employment.toInt).toOption
      turnover <- variables.get(turnover)
      turnoverInt <- Try(turnover.toInt).toOption
      prn <- variables.get(prn)
    } yield ReportingUnit(Rurn(rurn), optRuref, Ern(ern), optEntref, name, optTradingStyle, optLegalStatus,
      address1, optAddress2, optAddress3, optAddress4, optAddress5, postcode, sic07, employeesInt, employmentInt,
      turnoverInt, prn)
}
