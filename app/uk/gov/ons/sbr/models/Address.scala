package uk.gov.ons.sbr.models

import play.api.libs.json.Json
import io.swagger.annotations.ApiModelProperty

case class Address(
  @ApiModelProperty(value = "line1", dataType = "string", example = "21 High Street", required = true) line1: String,
  @ApiModelProperty(value = "line2", dataType = "string", required = false) line2: Option[String],
  @ApiModelProperty(value = "line3", dataType = "string", required = false) line3: Option[String],
  @ApiModelProperty(value = "line4", dataType = "string", required = false) line4: Option[String],
  @ApiModelProperty(value = "line5", dataType = "string", example = "Newport", required = false) line5: Option[String],
  @ApiModelProperty(value = "postcode", dataType = "string", example = "NP10 5XJ", required = true) postcode: String
)

object Address {
  implicit val writes = Json.writes[Address]
}
