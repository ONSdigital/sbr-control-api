package repository.hbase.legalunit

import repository.hbase.HBase.{ RowKeyDelimiter, Wildcard }
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.Ubrn

object LegalUnitQuery {
  def byRowKey(ern: Ern, ubrn: Ubrn): String =
    queryBy(ern, ubrnSelector = ubrn.value)

  def forAllWith(ern: Ern): String =
    queryBy(ern, ubrnSelector = Wildcard)

  private def queryBy(ern: Ern, ubrnSelector: String): String =
    Seq(Ern.reverse(ern), ubrnSelector).mkString(RowKeyDelimiter)
}
