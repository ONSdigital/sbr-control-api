package uk.gov.ons.sbr.models.unitlinks

case class UnitLinksNoPeriod(
  id: UnitId,
  unitType: UnitType,
  parents: Option[Map[UnitType, UnitId]],
  children: Option[Map[UnitId, UnitType]]
)
