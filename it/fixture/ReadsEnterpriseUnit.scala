package fixture

import play.api.libs.json.{Json, Reads}
import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.{Enterprise, Ern, Imputed, Turnover}

object ReadsEnterpriseUnit {
  implicit val ernReads: Reads[Ern] = Reads.StringReads.map(Ern(_))
  implicit val addressReads: Reads[Address] = Json.reads[Address]
  implicit val turnoverReads: Reads[Turnover] = Json.reads[Turnover]
  implicit val imputedReads: Reads[Imputed] = Json.reads[Imputed]
  implicit val enterpriseReads: Reads[Enterprise] = Json.reads[Enterprise]
}
