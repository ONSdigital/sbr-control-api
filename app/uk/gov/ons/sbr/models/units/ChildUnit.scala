package uk.gov.ons.sbr.models.units

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.{ Json, OFormat }

import uk.gov.ons.sbr.data.model.StatUnit

/**
 * Created by haqa on 27/09/2017.
 */
case class ChildUnit(
  @ApiModelProperty(example = "") id: String,
  unitType: String,
  children: Option[Seq[Map[String, String]]]
)

object ChildUnit {

  implicit val unitFormat: OFormat[ChildUnit] = Json.format[ChildUnit]

  def apply(x: StatUnit): ChildUnit = {
    ChildUnit(x.key, x.unitType, Option(x.children.map { v => Map("id" -> v.key, "unitType" -> v.unitType) }))
  }
}
