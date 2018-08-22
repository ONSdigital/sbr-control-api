package fixture

import play.api.libs.json.{Json, Reads}

import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.{EnterpriseLink, Ern}
import uk.gov.ons.sbr.models.localunit.{LocalUnit, Lurn}
import uk.gov.ons.sbr.models.reportingunit.{ReportingUnitLink, Rurn}

object ReadsLocalUnit {
  implicit val addressReads = Json.reads[Address]
  implicit val ernReads = Reads.StringReads.map(Ern(_))
  implicit val enterpriseLinkReads = Json.reads[EnterpriseLink]
  implicit val rurnReads = Reads.StringReads.map(Rurn(_))
  implicit val reportingUnitLinkReads = Json.reads[ReportingUnitLink]
  implicit val lurnReads = Reads.StringReads.map(Lurn(_))
  implicit val localUnitReads = Json.reads[LocalUnit]
}
