package repository.hbase.enterprise

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern

import repository.hbase.HBase.RowKeyDelimiter

object EnterpriseUnitRowKey {
  def apply(ern: Ern, period: Period): String = {
    val ernStr = Ern.reverse(ern)
    val periodStr = Period.asString(period)
    Seq(ernStr, periodStr).mkString(RowKeyDelimiter)
  }
}
