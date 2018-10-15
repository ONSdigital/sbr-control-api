package uk.gov.ons.sbr.models.enterprise

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ Json, OWrites }

case class Imputed(
  @ApiModelProperty(value = "Imputed Employees", dataType = "int", example = "10", required = false) employees: Option[Int],
  @ApiModelProperty(value = "Imputed Turnover", dataType = "int", example = "5000", required = false) turnover: Option[Int]
)

object Imputed {
  implicit val writes: OWrites[Imputed] = Json.writes[Imputed]
}