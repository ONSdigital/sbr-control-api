package uk.gov.ons.sbr.models.localunit

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.Json
import uk.gov.ons.sbr.models.enterprise.EnterpriseLink

case class LocalUnit(
  @ApiModelProperty(value = "Local Unit Reference Number (LURN)", dataType = "string", example = "1000000012", required = true) lurn: Lurn,
  @ApiModelProperty(value = "IDBR Local Unit Reference", dataType = "string", example = "9999999999", required = true) luref: String,
  @ApiModelProperty(value = "name", dataType = "string", example = "Company X", required = true) name: String,
  @ApiModelProperty(value = "tradingStyle", dataType = "", example = "A", required = true) tradingStyle: String,
  @ApiModelProperty(value = "UK Standard Industrial Classification 2007", dataType = "string", example = "21342", required = true) sic07: String,
  @ApiModelProperty(value = "Number of employees", dataType = "int", example = "100", required = true) employees: Int,
  @ApiModelProperty(value = "A container for links to the associated Enterprise", dataType = "uk.gov.ons.sbr.models.enterprise.EnterpriseLink", required = true) enterprise: EnterpriseLink,
  @ApiModelProperty(value = "A container for address details", dataType = "uk.gov.ons.sbr.models.localunit.Address", required = true) address: Address
)

object LocalUnit {
  implicit val writes = Json.writes[LocalUnit]
}