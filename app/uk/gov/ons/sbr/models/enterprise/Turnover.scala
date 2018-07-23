package uk.gov.ons.sbr.models.enterprise

import play.api.libs.json.{ Json, OWrites }
import io.swagger.annotations.ApiModelProperty

case class Turnover(
  @ApiModelProperty(value = "Sum of rep VAT turnover for contained rep VAT groups", dataType = "int", example = "99", required = false) containedTurnover: Option[Int],
  @ApiModelProperty(value = "Sum of turnover for standard vats", dataType = "int", example = "99", required = false) standardTurnover: Option[Int],
  @ApiModelProperty(value = "Sum of rep VAT group turnover", dataType = "int", example = "99", required = false) groupTurnover: Option[Int],
  @ApiModelProperty(value = "Apportioned rep VAT turnover based on employees", dataType = "int", example = "99", required = false) apportionedTurnover: Option[Int],
  @ApiModelProperty(value = "Sum of all (standard + contained + apportioned) turnover values for that enterprise", dataType = "int", example = "99", required = false) enterpriseTurnover: Option[Int]
)

object Turnover {
  implicit val writes: OWrites[Turnover] = Json.writes[Turnover]
}