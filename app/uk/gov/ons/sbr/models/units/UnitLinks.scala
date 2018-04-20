package uk.gov.ons.sbr.models.units

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ Json, OFormat }
import uk.gov.ons.sbr.models.DataUnit

case class UnitLinks(
  @ApiModelProperty(value = "Unit Link identifier", example = "1000000012~ENT~201802", dataType = "String", required = true) id: String,
  period: String,
  @ApiModelProperty(value = "A map of parents of returned id [Type, Value]", example = "",
    dataType = "Map[String,String]") parents: Option[Map[String, String]],
  @ApiModelProperty(value = "A string of all related children", example = "") children: Option[Map[String, String]],
  @ApiModelProperty(value = "Type of Unit returned", example = "") unitType: String
) extends DataUnit[String]

object UnitLinks {

  implicit val unitFormat: OFormat[UnitLinks] = Json.format[UnitLinks]
}