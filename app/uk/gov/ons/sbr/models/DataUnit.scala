package uk.gov.ons.sbr.models

import com.google.inject.ImplementedBy
import io.swagger.annotations.ApiModelProperty

import uk.gov.ons.sbr.models.units.EnterpriseUnit

/**
 * Created by haqa on 20/09/2017.
 */
@ImplementedBy(classOf[EnterpriseUnit])
trait DataUnit[X] {
  @ApiModelProperty(value = "Unit identifier", required = true, hidden = false) val id: X
}

