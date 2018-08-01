package uk.gov.ons.sbr.models.localunit

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ Json, OWrites }

import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.enterprise.EnterpriseLink
import uk.gov.ons.sbr.models.reportingunit.ReportingUnitLink

case class LocalUnit(
  @ApiModelProperty(value = "Local Unit Reference Number (LURN)", dataType = "string", example = "1000000012", required = true) lurn: Lurn,
  @ApiModelProperty(value = "IDBR Local Unit Reference", dataType = "string", example = "9999999999", required = false) luref: Option[String],
  @ApiModelProperty(value = "A container for links to the associated Enterprise", dataType = "uk.gov.ons.sbr.models.enterprise.EnterpriseLink", required = true) enterprise: EnterpriseLink,
  @ApiModelProperty(value = "A container for links to the associated Reporting Unit", dataType = "uk.gov.ons.sbr.models.reportingunit.ReportingUnitLink", required = true) reportingUnit: ReportingUnitLink,
  @ApiModelProperty(value = "name", dataType = "string", example = "Big Box Cereal", required = true) name: String,
  @ApiModelProperty(value = "tradingStyle", dataType = "string", example = "Big Box Cereal Ltd", required = false) tradingStyle: Option[String],
  @ApiModelProperty(value = "A container for address details", dataType = "uk.gov.ons.sbr.models.Address", required = true) address: Address,
  @ApiModelProperty(value = "Primary Standard Industrial Classification (SIC)", dataType = "string", example = "10612", required = true) sic07: String,
  @ApiModelProperty(value = "Number of employees", dataType = "int", example = "100", required = true) employees: Int
)

object LocalUnit {
  implicit val writes: OWrites[LocalUnit] = Json.writes[LocalUnit]
}