package repository.hbase.unitlinks

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitType }

import repository.hbase.HBase.{ RowKeyDelimiter, Wildcard }

object UnitLinksRowKey {

  val unitLinksRowKeyLength = 3

  val unitIdIndex = 0
  val unitTypeIndex = 1
  val unitPeriodIndex = 2

  val split: (String) => List[String] = (rowKey: String) =>
    rowKey.split(RowKeyDelimiter).toList

  def apply(id: String): String =
    Seq(id, Wildcard).mkString(RowKeyDelimiter)

  def apply(id: UnitId, unitType: UnitType, period: Period): String =
    Seq(id, UnitType.toAcronym(unitType), Period.asString(period)).mkString(RowKeyDelimiter)
}
