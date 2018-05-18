package fixture

import play.api.libs.json.{ Json, Reads }
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.reportingunit.{ ReportingUnit, Rurn }

object ReadsReportingUnit {
  implicit val rurnReads = Reads.StringReads.map(Rurn(_))
  implicit val ernReads = Reads.StringReads.map(Ern(_))
  implicit val reportingUnitReads = Json.reads[ReportingUnit]
}
