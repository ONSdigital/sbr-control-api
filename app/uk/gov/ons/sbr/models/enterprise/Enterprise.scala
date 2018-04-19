package uk.gov.ons.sbr.models.enterprise

import play.api.libs.json.{ Json, OWrites }
import io.swagger.annotations.ApiModelProperty

case class Enterprise(
  @ApiModelProperty(value = "Enterprise Reference Number (ERN)", dataType = "string", example = "1000000012", required = true) ern: Ern,
  @ApiModelProperty(value = "IDBR Enterprise Reference", dataType = "string", example = "9999999999", required = false) entref: String,
  @ApiModelProperty(value = "name", dataType = "string", example = "Company X", required = true) name: String,
  @ApiModelProperty(value = "postcode", dataType = "string", example = "NP10 5XJ", required = true) postcode: String,
  @ApiModelProperty(value = "Legal status", dataType = "string", example = "NP10 5XJ", required = true) legalStatus: String,
  @ApiModelProperty(value = "Number of employees (source: PAYE)", dataType = "int", example = "100", required = false) employees: Option[Int],
  @ApiModelProperty(value = "Sum of employees for latest period (source: PAYE)", dataType = "int", example = "100", required = false) jobs: Option[Int]
)

object Enterprise {
  implicit val writes: OWrites[Enterprise] = Json.writes[Enterprise]
}
