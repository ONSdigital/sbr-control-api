package repository.hbase.unitlinks

import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitType }

object UnitLinksQualifier {
  val ChildPrefix = "c_"
  val ParentPrefix = "p_"

  def toParent(parentType: UnitType): String =
    ParentPrefix + UnitType.toAcronym(parentType)

  def toChild(id: UnitId): String =
    ChildPrefix + id.value
}
