package it.fixture

import play.api.libs.json.{ Json, Reads }

import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern }

object ReadsEnterpriseUnit {
  implicit val ernReads: Reads[Ern] = Reads.StringReads.map(Ern(_))
  implicit val addressReads: Reads[Address] = Json.reads[Address]
  implicit val enterpriseReads: Reads[Enterprise] = Json.reads[Enterprise]
}
