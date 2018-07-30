package repository.hbase

import java.time.Month.JULY

import org.scalatest.{ FreeSpec, Matchers }
import uk.gov.ons.sbr.models.Period

class PeriodTableNameSpec extends FreeSpec with Matchers {

  "A period table name" - {
    "is an entity name with a period suffix" in {
      PeriodTableName("legal_unit", Period.fromYearMonth(2018, JULY)) shouldBe "legal_unit_201807"
    }
  }
}
