package uk.gov.ons.sbr.models.reportingunit

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.Json

case class ReportingUnitLink(
  @ApiModelProperty(value = "Reporting Unit Reference Number (Rurn)", dataType = "string", example = "10000000121", required = true) rurn: Rurn,
  @ApiModelProperty(value = "IDBR Reporting Unit Reference", dataType = "string", example = "99999999991", required = false) ruref: Option[String]
)

object ReportingUnitLink {
  implicit val writes = Json.writes[ReportingUnitLink]
}