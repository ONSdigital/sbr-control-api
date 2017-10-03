package uk.gov.ons.sbr.models

import io.swagger.annotations.ApiModelProperty

/**
 * Created by haqa on 20/09/2017.
 */
trait DataUnit[X] {
  @ApiModelProperty(value = "Unit identifier", required = true, hidden = false) val id: X
}

