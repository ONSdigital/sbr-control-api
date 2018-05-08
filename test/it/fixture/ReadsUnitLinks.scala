package it.fixture

import play.api.libs.json.{ Json, Reads }

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{ UnitLinks, UnitType }

object ReadsUnitLinks {
  implicit val periodReads: Reads[Period] = Reads.StringReads.map(Period.fromString)
  implicit val unitTypeReads: Reads[UnitType] = Reads.StringReads.map(UnitType.fromAcronym)
  implicit val unitLinksReads: Reads[UnitLinks] = Json.reads[UnitLinks]
}
