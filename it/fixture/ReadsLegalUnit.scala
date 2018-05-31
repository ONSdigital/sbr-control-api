package fixture

import play.api.libs.json.{ Json, Reads }
import uk.gov.ons.sbr.models.enterprise.{ EnterpriseLink, Ern }
import uk.gov.ons.sbr.models.legalunit.{ LegalUnit, UBRN }

object ReadsLegalUnit {
  implicit val ernReads = Reads.StringReads.map(Ern(_))
  implicit val enterpriseLinkReads = Json.reads[EnterpriseLink]
  implicit val UBRNReads = Reads.StringReads.map(UBRN(_))
  implicit val legalUnitReads = Json.reads[LegalUnit]
}