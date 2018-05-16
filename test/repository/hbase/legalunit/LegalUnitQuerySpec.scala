package repository.hbase.legalunit

import java.time.Month.MARCH

import org.scalatest.{ FreeSpec, Matchers }
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.UBRN

class LegalUnitQuerySpec extends FreeSpec with Matchers {
  "A Legal Unit query" - {
    "should contain the exact Legal Unit reference (UBRN)" - {
      "when requesting the legal unit with a specific row key" in {
        LegalUnitQuery.byRowKey(Ern("1000000014"), Period.fromYearMonth(2018, MARCH), UBRN("1111111100000000")) shouldBe "4100000001~201803~1111111100000000"
      }
    }

    "should contain a wildcard for the Legal Unit reference (UBRN)" - {
      "when requesting all legal units for a given enterprise and period" in {
        LegalUnitQuery.forAllWith(ern = Ern("1000000014"), period = Period.fromYearMonth(2018, MARCH)) shouldBe "4100000001~201803~*"
      }
    }
  }
}

