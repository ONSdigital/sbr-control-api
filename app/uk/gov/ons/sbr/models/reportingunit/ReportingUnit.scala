package uk.gov.ons.sbr.models.reportingunit

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ Json, OWrites }

case class ReportingUnit(
  @ApiModelProperty(value = "Reporting Unit Reference Number (RURN)", dataType = "string", example = "33000000000", required = true) rurn: Rurn,
  @ApiModelProperty(value = "IDBR Reporting Unit Reference", dataType = "string", example = "9999999999", required = false) ruref: Option[String]
)

object ReportingUnit {
  implicit val writes: OWrites[ReportingUnit] = Json.writes[ReportingUnit]
}
