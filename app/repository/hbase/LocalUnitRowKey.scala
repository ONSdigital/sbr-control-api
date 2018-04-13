package repository.hbase

import repository.hbase.HBase.RowKeyDelimiter
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.Lurn

object LocalUnitRowKey {
  def apply(ern: Ern, period: Period, lurn: Lurn): String = {
    val ernStr = Ern.reverse(ern)
    val periodStr = Period.asString(period)
    val lurnStr = lurn.value
    Seq(ernStr, periodStr, lurnStr).mkString(RowKeyDelimiter)
  }
}
