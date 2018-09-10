package uk.gov.ons.sbr.models

import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitType }

case class UnitKey(unitId: UnitId, unitType: UnitType, period: Period)
