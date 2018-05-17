package repository.hbase.unitlinks

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitType }

import repository.hbase.HBase.RowKeyDelimiter
import com.typesafe.scalalogging.Logger

object UnitLinksRowKey {

  val numberOfUnitLinksRowKeyComponents = 3

  val unitIdIndex = 0
  val unitTypeIndex = 1
  val unitPeriodIndex = 2

  def split(rowKey: String): List[String] = rowKey.split(RowKeyDelimiter).toList

  def splitRowKey(rowKey: String)(logger: Logger): Option[List[String]] = {
    val partitionedRowKey = split(rowKey)
    if (partitionedRowKey.length != numberOfUnitLinksRowKeyComponents) {
      logger.warn(s"Failure to produce UnitLinks, caused by rowKey [$rowKey] is of invalid segment size " +
        s"[${partitionedRowKey.length}] when expected [$numberOfUnitLinksRowKeyComponents]")
      None
    } else Some(partitionedRowKey)
  }

  def apply(id: UnitId, unitType: UnitType, period: Period): String =
    Seq(id.value, UnitType.toAcronym(unitType), Period.asString(period)).mkString(RowKeyDelimiter)
}
