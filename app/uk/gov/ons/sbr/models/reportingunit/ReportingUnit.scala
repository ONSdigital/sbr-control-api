package uk.gov.ons.sbr.models.reportingunit

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json._
import uk.gov.ons.sbr.models.WritesBigDecimal
import uk.gov.ons.sbr.models.enterprise.Ern

case class ReportingUnit(
  @ApiModelProperty(value = "Reporting Unit Reference Number (RURN)", dataType = "string", example = "33000000000", required = true) rurn: Rurn,
  @ApiModelProperty(value = "IDBR Reporting Unit Reference", dataType = "string", example = "9999999999", required = false) ruref: Option[String],
  @ApiModelProperty(value = "Enterprise Reference Number (ERN)", dataType = "string", example = "1000000012", required = true) ern: Ern,
  @ApiModelProperty(value = "IDBR Enterprise Reference", dataType = "string", example = "9999999999", required = false) entref: Option[String],
  @ApiModelProperty(value = "Reporting Unit name", dataType = "string", example = "Big Box Cereal", required = true) name: String,
  @ApiModelProperty(value = "Reporting Unit trading style", dataType = "string", example = "Big Box Cereal Ltd", required = false) tradingStyle: Option[String],
  @ApiModelProperty(value = "Reporting Unit legal status", dataType = "string", example = "1", required = false) legalStatus: Option[String],
  @ApiModelProperty(value = "Reporting Unit address line 1", dataType = "string", example = "1 Brook Court", required = true) address1: String,
  @ApiModelProperty(value = "Reporting Unit address line 2", dataType = "string", example = "Bow Bridge", required = false) address2: Option[String],
  @ApiModelProperty(value = "Reporting Unit address line 3", dataType = "string", example = "Wateringbury", required = false) address3: Option[String],
  @ApiModelProperty(value = "Reporting Unit address line 4", dataType = "string", example = "Maidstone", required = false) address4: Option[String],
  @ApiModelProperty(value = "Reporting Unit address line 5", dataType = "string", example = "Kent", required = false) address5: Option[String],
  @ApiModelProperty(value = "Reporting Unit post code", dataType = "string", example = "NP10 ABC", required = true) postcode: String,
  @ApiModelProperty(value = "Reporting Unit sic07", dataType = "string", example = "71122", required = true) sic07: String,
  @ApiModelProperty(value = "Reporting Unit employees", dataType = "int", example = "5", required = true) employees: Int,
  @ApiModelProperty(value = "Reporting Unit employment", dataType = "int", example = "6", required = true) employment: Int,
  @ApiModelProperty(value = "Reporting Unit turnover", dataType = "int", example = "4566", required = true) turnover: Int,
  @ApiModelProperty(value = "Permanent Random Number (PRN)", dataType = "string", example = "0.016587362", required = true) prn: BigDecimal
)

object ReportingUnit {
  private implicit val writesBigDecimal: Writes[BigDecimal] = WritesBigDecimal
  implicit val writes: OWrites[ReportingUnit] = Json.writes[ReportingUnit]
}