package fixture

import play.api.libs.json.{Json, Reads}
import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.{EnterpriseLink, Ern}
import uk.gov.ons.sbr.models.legalunit.{Crn, LegalUnit, Ubrn, Uprn}

object ReadsLegalUnit {
  implicit val addressReads = Json.reads[Address]
  implicit val ernReads = Reads.StringReads.map(Ern(_))
  implicit val enterpriseLinkReads = Json.reads[EnterpriseLink]
  implicit val ubrnReads = Reads.StringReads.map(Ubrn(_))
  implicit val crnReads = Reads.StringReads.map(Crn(_))
  implicit val uprnReads = Reads.StringReads.map(Uprn(_))
  implicit val legalUnitReads = Json.reads[LegalUnit]
}