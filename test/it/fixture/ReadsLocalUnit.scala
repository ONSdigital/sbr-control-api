package it.fixture

import play.api.libs.json.{ Json, Reads }
import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.localunit.{ Address, LocalUnit, Lurn }

object ReadsLocalUnit {
  implicit val addressReads = Json.reads[Address]
  implicit val ernReads = Reads.StringReads.map(Ern(_))
  implicit val enterpriseLinkReads = Json.reads[EnterpriseLink]
  implicit val lurnReads = Reads.StringReads.map(Lurn(_))
  implicit val localUnitReads = Json.reads[LocalUnit]
}
