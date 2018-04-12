package uk.gov.ons.sbr.models.enterprise

import play.api.libs.json.{ Json, OWrites }

case class Enterprise(
  ern: Ern,
  entref: String,
  name: String,
  postcode: String,
  legalStatus: String,
  employees: Option[Int],
  jobs: Option[Int]
)

object Enterprise {
  implicit val writes: OWrites[Enterprise] = Json.writes[Enterprise]
}
