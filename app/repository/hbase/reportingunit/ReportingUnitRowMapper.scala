package repository.hbase.reportingunit

import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.reportingunit.ReportingUnitColumns._
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnit, Rurn }

object ReportingUnitRowMapper extends RowMapper[ReportingUnit] {

  override def fromRow(variables: Row): Option[ReportingUnit] =
    for {
      rurn <- variables.fields.get(rurn)
      optRuref = variables.fields.get(ruref)
    } yield ReportingUnit(Rurn(rurn), optRuref)
}
