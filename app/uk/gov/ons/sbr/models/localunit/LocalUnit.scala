package uk.gov.ons.sbr.models.localunit

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ Json, OWrites, Writes }
import uk.gov.ons.sbr.models.{ Address, WritesBigDecimal }
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
  @ApiModelProperty(value = "Number of employees", dataType = "int", example = "100", required = true) employees: Int,
  @ApiModelProperty(value = "Employment", dataType = "int", example = "101", required = true) employment: Int,
  @ApiModelProperty(value = "Permanent Random Number (PRN)", dataType = "string", example = "0.016587362", required = true) prn: BigDecimal,
  @ApiModelProperty(value = "Region", dataType = "string", example = "E12000001", required = true) region: String
)

object LocalUnit {
  private implicit val writesBigDecimal: Writes[BigDecimal] = WritesBigDecimal
  implicit val writes: OWrites[LocalUnit] = Json.writes[LocalUnit]
}