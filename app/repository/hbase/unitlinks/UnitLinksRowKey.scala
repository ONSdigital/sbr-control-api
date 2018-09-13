package repository.hbase.unitlinks

import repository.hbase.HBase.RowKeyDelimiter
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitType }

object UnitLinksRowKey {
  def apply(id: UnitId, unitType: UnitType): String =
    Seq(UnitType.toAcronym(unitType), id.value).mkString(RowKeyDelimiter)

  def unapply(rowKey: String): Option[(UnitId, UnitType)] =
    rowKey.split(RowKeyDelimiter).toList match {
      case (unitTypeStr :: unitIdStr :: Nil) =>
        UnitType.fromString(unitTypeStr).toOption.map { unitType =>
          UnitId(unitIdStr) -> unitType
        }
      case _ => None
    }
}
