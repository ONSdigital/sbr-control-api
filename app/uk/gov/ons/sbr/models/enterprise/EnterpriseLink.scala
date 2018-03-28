package uk.gov.ons.sbr.models.enterprise

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.Json

case class EnterpriseLink(
  @ApiModelProperty(value = "Enterprise Reference Number (ERN)", dataType = "string", example = "1000000012", required = true) ern: Ern,
  @ApiModelProperty(value = "IDBR Enterprise Reference", dataType = "string", example = "9999999999", required = true) entref: String
)

object EnterpriseLink {
  implicit val writes = Json.writes[EnterpriseLink]
}