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
  //  def toMap(a: Address): Map[String, String] = Map(
  //    "line1" -> a.line1,
  //    "line2" -> a.line2,
  //    "line3" -> a.line3,
  //    "line4" -> a.line4,
  //    "line5" -> a.line5,
  //    "postcode" -> a.postcode
  //  )

  implicit val address: OFormat[Address] = Json.format[Address]

}

