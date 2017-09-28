package uk.gov.ons.sbr.models

import io.swagger.annotations.ApiModelProperty

/**
 * Created by haqa on 20/09/2017.
 */
trait DataUnit[X] {
  @ApiModelProperty(value = "Unit identifier", required = true, hidden = false) val id: X
  @ApiModelProperty(value = "A map of parents of returned id [Type, Value]", example = "",
    dataType = "Map[String,String]") val parents: Option[Map[String, String]]
  @ApiModelProperty(value = "A string of all related children", example = "") val children: Option[Map[String, String]]
  @ApiModelProperty(value = "Type of Unit returned", example = "") val unitType: String
}

