package repository.hbase.unitlinks

import java.time.Month.MARCH

import org.scalatest.{ FreeSpec, Matchers }

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitType }

class UnitLinksRowKeySpec extends FreeSpec with Matchers {

  private val Id = "109085670091"

  "A Unit Links Row Key" - {
    "should contain the exact unit identifier (id), the unit type of the unit (of UnitType) and period (as yyyyMM)" - {
      "when requesting a specific row key containing a known unit with a specific period" in {
        UnitLinksRowKey(UnitId(Id), UnitType.LegalUnit, Period.fromYearMonth(2018, MARCH)) shouldBe "109085670091~LEU~201803"
      }
    }

    "should contain a wildcard for the unit identifier (id)" - {
      "when requesting a unit of unknown type" in {
        UnitLinksRowKey(UnitId(Id)) shouldBe "109085670091~*"
      }
    }
  }

}
