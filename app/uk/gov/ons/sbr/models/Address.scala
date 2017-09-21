package uk.gov.ons.sbr.models.attributes

import io.swagger.annotations.ApiModelProperty

import play.api.libs.json.{ Json, OFormat }

/**
 * Created by Ameen on 15/07/2017.
 */
case class Address(
  @ApiModelProperty(value = "Street and optional door number", example = "101 Long Street") line1: Option[String],
  @ApiModelProperty(value = "Optional field for building or apartment name", example = "Little Winsor") line2: Option[String],
  @ApiModelProperty(value = "Town of address", example = "Bury") line3: Option[String],
  @ApiModelProperty(value = "City of address", example = "Manchester") line4: Option[String],
  @ApiModelProperty(value = "County of address", example = "Gtr Manchester") line5: Option[String],
  @ApiModelProperty(value = "A post specific to the address of entity") postcode: Option[String]

)

object Address {

  implicit val address: OFormat[Address] = Json.format[Address]

}

