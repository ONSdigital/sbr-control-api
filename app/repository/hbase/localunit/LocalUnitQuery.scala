package repository.hbase.localunit

import repository.hbase.HBase.{ RowKeyDelimiter, Wildcard }
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.Lurn

object LocalUnitQuery {
  def byRowKey(ern: Ern, lurn: Lurn): String =
    queryBy(ern, lurnSelector = lurn.value)

  def forAllWith(ern: Ern): String =
    queryBy(ern, lurnSelector = Wildcard)

  private def queryBy(ern: Ern, lurnSelector: String): String =
    Seq(Ern.reverse(ern), lurnSelector).mkString(RowKeyDelimiter)
}
