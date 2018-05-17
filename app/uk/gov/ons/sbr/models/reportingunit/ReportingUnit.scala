package uk.gov.ons.sbr.models.reportingunit

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ Json, OWrites }
import uk.gov.ons.sbr.models.enterprise.Ern

case class ReportingUnit(
  @ApiModelProperty(value = "Reporting Unit Reference Number (RURN)", dataType = "string", example = "33000000000", required = true) rurn: Rurn,
  @ApiModelProperty(value = "IDBR Reporting Unit Reference", dataType = "string", example = "9999999999", required = false) ruref: Option[String],
  @ApiModelProperty(value = "Enterprise Reference Number (ERN)", dataType = "string", example = "1000000012", required = true) ern: Ern,
  @ApiModelProperty(value = "IDBR Enterprise Reference", dataType = "string", example = "9999999999", required = false) entref: Option[String],
  @ApiModelProperty(value = "Reporting Unit name", dataType = "string", example = "Company Ltd", required = true) name: String,
  @ApiModelProperty(value = "Reporting Unit trading style", dataType = "string", example = "A", required = false) tradingStyle: Option[String],
  @ApiModelProperty(value = "Reporting Unit legal status", dataType = "string", example = "A", required = false) legalStatus: Option[String],
  @ApiModelProperty(value = "Reporting Unit address line 1", dataType = "string", example = "10 Long Road", required = true) address1: String,
  @ApiModelProperty(value = "Reporting Unit address line 2", dataType = "string", example = "Newport", required = false) address2: Option[String],
  @ApiModelProperty(value = "Reporting Unit address line 3", dataType = "string", example = "Gwent", required = false) address3: Option[String],
  @ApiModelProperty(value = "Reporting Unit address line 4", dataType = "string", example = "South Wales", required = false) address4: Option[String],
  @ApiModelProperty(value = "Reporting Unit address line 5", dataType = "string", example = "UK", required = false) address5: Option[String],
  @ApiModelProperty(value = "Reporting Unit post code", dataType = "string", example = "NP10 ABC", required = true) postcode: String,
  @ApiModelProperty(value = "Reporting Unit sic07", dataType = "string", example = "10000", required = true) sic07: String,
  @ApiModelProperty(value = "Reporting Unit employees", dataType = "int", example = "13000", required = true) employees: Int,
  @ApiModelProperty(value = "Reporting Unit employment", dataType = "int", example = "100", required = true) employment: Int,
  @ApiModelProperty(value = "Reporting Unit turnover", dataType = "int", example = "1000000", required = true) turnover: Int,
  @ApiModelProperty(value = "Reporting Unit prn", dataType = "float", example = "0.2", required = true) prn: Float
)

object ReportingUnit {
  implicit val writes: OWrites[ReportingUnit] = Json.writes[ReportingUnit]
}