package uk.gov.ons.sbr.models.units

import scala.collection.JavaConversions._

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ Json, OFormat }

import uk.gov.ons.sbr.models.DataUnit

/**
 * Created by haqa on 08/08/2017.
 */

case class UnitLinks(
  @ApiModelProperty(example = "", dataType = "String") id: String,
  @ApiModelProperty(value = "A map of parents of returned id [Type, Value]", example = "",
    dataType = "Map[String,String]") parents: Option[Map[String, String]],
  @ApiModelProperty(value = "A string of all related children", example = "") children: Option[Map[String, String]],
  @ApiModelProperty(value = "Type of Unit returned", example = "") unitType: String
) extends DataUnit[String]

object UnitLinks {

  implicit val unitFormat: OFormat[UnitLinks] = Json.format[UnitLinks]
}