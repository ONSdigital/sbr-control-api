package uk.gov.ons.sbr.models.localunit

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.Json

case class Address(
  @ApiModelProperty(value = "line1", dataType = "string", example = "21 High Street", required = true) line1: String,
  @ApiModelProperty(value = "line2", dataType = "string", required = true, notes = "A mandatory value that may be empty") line2: String,
  @ApiModelProperty(value = "line3", dataType = "string", required = true, notes = "A mandatory value that may be empty") line3: String,
  @ApiModelProperty(value = "line4", dataType = "string", required = true, notes = "A mandatory value that may be empty") line4: String,
  @ApiModelProperty(value = "line5", dataType = "string", example = "Newport", required = true) line5: String,
  @ApiModelProperty(value = "postcode", dataType = "string", example = "NP10 5XJ", required = true) postcode: String
)

object Address {
  implicit val writes = Json.writes[Address]
}
