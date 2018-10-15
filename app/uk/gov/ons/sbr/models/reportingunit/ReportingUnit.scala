package uk.gov.ons.sbr.models.reportingunit

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json._
import uk.gov.ons.sbr.models.enterprise.EnterpriseLink
import uk.gov.ons.sbr.models.{ Address, WritesBigDecimal }

case class ReportingUnit(
  @ApiModelProperty(value = "Reporting Unit Reference Number (RURN)", dataType = "string", example = "33000000000", required = true) rurn: Rurn,
  @ApiModelProperty(value = "IDBR Reporting Unit Reference", dataType = "string", example = "9999999999", required = false) ruref: Option[String],
  @ApiModelProperty(value = "A container for links to the associated Enterprise", dataType = "uk.gov.ons.sbr.models.enterprise.EnterpriseLink", required = true) enterprise: EnterpriseLink,
  @ApiModelProperty(value = "Reporting unit name", dataType = "string", example = "Big Box Cereal", required = true) name: String,
  @ApiModelProperty(value = "Trading style", dataType = "string", example = "Big Box Cereal Ltd", required = false) tradingStyle: Option[String],
  @ApiModelProperty(value = "Legal status", dataType = "string", example = "1", required = true) legalStatus: String,
  @ApiModelProperty(value = "A container for address details", dataType = "uk.gov.ons.sbr.models.Address", required = true) address: Address,
  @ApiModelProperty(value = "Standard Industrial Classification (SIC)", dataType = "string", example = "71122", required = true) sic07: String,
  @ApiModelProperty(value = "Employees", dataType = "int", example = "5", required = true) employees: Int,
  @ApiModelProperty(value = "Employment", dataType = "int", example = "6", required = true) employment: Int,
  @ApiModelProperty(value = "Turnover", dataType = "int", example = "4566", required = true) turnover: Int,
  @ApiModelProperty(value = "Permanent Random Number (PRN)", dataType = "string", example = "0.016587362", required = true) prn: BigDecimal,
  @ApiModelProperty(value = "Region", dataType = "string", example = "E12000001", required = true) region: String
)

object ReportingUnit {
  private implicit val writesBigDecimal: Writes[BigDecimal] = WritesBigDecimal
  implicit val writes: OWrites[ReportingUnit] = Json.writes[ReportingUnit]
}