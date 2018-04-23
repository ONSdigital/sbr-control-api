package uk.gov.ons.sbr.models

import com.google.inject.ImplementedBy
import uk.gov.ons.sbr.models.units.EnterpriseUnit

@ImplementedBy(classOf[EnterpriseUnit])
trait DataUnit[X] {
  val id: X
}

