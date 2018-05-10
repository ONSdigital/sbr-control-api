package repository.hbase.unitlinks

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.UnitType

import repository.hbase.HBase.{ RowKeyDelimiter, Wildcard }

object UnitLinksRowKey {

  val unitLinksRowKeyLength = 3

  val unitIdIndex = 1
  val unitTypeIndex = 2
  val unitPeriodIndex = 3

  val split: (String) => Array[String] = (rowKey: String) =>
    rowKey.split(RowKeyDelimiter)

  def apply(id: String): String =
    Seq(id, Wildcard).mkString(RowKeyDelimiter)

  def apply(id: String, unitType: UnitType, period: Period): String =
    Seq(id, UnitType.toAcronym(unitType), Period.asString(period)).mkString(RowKeyDelimiter)
}
