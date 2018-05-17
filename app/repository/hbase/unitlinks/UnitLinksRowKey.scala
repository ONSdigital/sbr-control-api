package repository.hbase.unitlinks

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitType }

import repository.hbase.HBase.{ RowKeyDelimiter, Wildcard }

object UnitLinksRowKey {

  val UnitLinksRowKeyLength = 3

  val UnitIdIndex = 0
  val UnitTypeIndex = 1
  val UnitPeriodIndex = 2

  val split: (String) => List[String] = (rowKey: String) =>
    rowKey.split(RowKeyDelimiter).toList

  def apply(id: UnitId): String =
    Seq(id.value, Wildcard).mkString(RowKeyDelimiter)

  def apply(id: UnitId, unitType: UnitType, period: Period): String =
    Seq(id.value, UnitType.toAcronym(unitType), Period.asString(period)).mkString(RowKeyDelimiter)
}
