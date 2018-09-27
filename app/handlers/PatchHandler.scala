package handlers

import uk.gov.ons.sbr.models.UnitKey
import uk.gov.ons.sbr.models.patch.Patch

trait PatchHandler[A] {
  def apply(unitKey: UnitKey, patch: Patch): A
}
