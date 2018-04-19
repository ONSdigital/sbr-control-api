package repository.hbase.localunit

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.Lurn

import repository.hbase.HBase.RowKeyDelimiter

object LocalUnitRowKey {
  def apply(ern: Ern, period: Period, lurn: Lurn): String = {
    val ernStr = Ern.reverse(ern)
    val periodStr = Period.asString(period)
    val lurnStr = lurn.value
    Seq(ernStr, periodStr, lurnStr).mkString(RowKeyDelimiter)
  }
}
