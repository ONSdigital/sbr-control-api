package uk.gov.ons.sbr.models.enterprise

import play.api.libs.json.{ Json, OWrites }
import io.swagger.annotations.ApiModelProperty

case class Turnover(
  @ApiModelProperty(value = "Sum of turnover for contained rep vats", dataType = "int", example = "99") containedTurnover: Option[Int],
  @ApiModelProperty(value = "Sum of turnover for standard vats", dataType = "int", example = "99") standardTurnover: Option[Int],
  @ApiModelProperty(value = "Sum of vat group turnover", dataType = "int", example = "99") groupTurnover: Option[Int],
  @ApiModelProperty(value = "Apportioned turnover based on employees", dataType = "int", example = "99") apportionedTurnover: Option[Int],
  @ApiModelProperty(value = "Sum of all turnover values for that enterprise", dataType = "int", example = "99") enterpriseTurnover: Option[Int]
)

object Turnover {
  implicit val writes: OWrites[Turnover] = Json.writes[Turnover]
}