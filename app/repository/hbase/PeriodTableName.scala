package repository.hbase

import uk.gov.ons.sbr.models.Period

object PeriodTableName {
  def apply(entityName: String, period: Period): String =
    Seq(entityName, Period.asString(period)).mkString("_")
}
