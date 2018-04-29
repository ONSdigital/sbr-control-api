package repository.hbase.unitlinks

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.UnitType

import repository.hbase.HBase.{ RowKeyDelimiter, Wildcard }

object UnitLinksRowKey {
  def apply(id: String): String =
    Seq(id, Wildcard).mkString(RowKeyDelimiter)

  def apply(id: String, unitType: UnitType, period: Period): String =
    Seq(id, unitType.toString, Period.asString(period)).mkString(RowKeyDelimiter)
}
