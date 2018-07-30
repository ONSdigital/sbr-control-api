package repository.hbase.legalunit

import org.scalatest.{ FreeSpec, Matchers }
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.legalunit.Ubrn

class LegalUnitQuerySpec extends FreeSpec with Matchers {
  "A Legal Unit query" - {
    "should contain the exact Legal Unit reference (UBRN)" - {
      "when requesting the legal unit with a specific row key" in {
        LegalUnitQuery.byRowKey(Ern("1000000014"), Ubrn("1111111100000000")) shouldBe "4100000001~1111111100000000"
      }
    }

    "should contain a wildcard for the Legal Unit reference (UBRN)" - {
      "when requesting all legal units for a given enterprise" in {
        LegalUnitQuery.forAllWith(ern = Ern("1000000014")) shouldBe "4100000001~*"
      }
    }
  }
}

