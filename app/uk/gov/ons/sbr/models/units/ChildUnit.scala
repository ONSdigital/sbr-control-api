package uk.gov.ons.sbr.models.units

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ Json, OFormat }

/**
 * Created by haqa on 27/09/2017.
 */
case class ChildUnit(
  @ApiModelProperty(example = "") id: String,
  unitType: String,
  children: Map[String, String]
)

object ChildUnit {

  implicit val unitFormat: OFormat[ChildUnit] = Json.format[ChildUnit]
}
