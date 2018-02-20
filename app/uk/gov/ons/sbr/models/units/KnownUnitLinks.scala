package uk.gov.ons.sbr.models.units

import scala.collection.JavaConversions._

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ Json, OFormat }

import uk.gov.ons.sbr.models.DataUnit

/**
 * Created by haqa on 21/09/2017.
 */
case class KnownUnitLinks(
  @ApiModelProperty(example = "", dataType = "String") id: String,
  @ApiModelProperty(value = "A map of parents of returned id [Type, Value]", example = "",
    dataType = "Map[String,String]") parents: Option[Map[String, String]],
  @ApiModelProperty(value = "A string of all related children", example = "") children: Option[Map[String, String]]
) extends DataUnit[String]

object KnownUnitLinks {

  implicit val unitFormat: OFormat[KnownUnitLinks] = Json.format[KnownUnitLinks]
}

