package uk.gov.ons.sbr.models

import play.api.libs.json.{ JsString, JsValue, Writes }

private[models] object WritesBigDecimal extends Writes[BigDecimal] {
  def writes(bd: BigDecimal): JsValue =
    JsString(bd.toString())
}
