package repository.hbase.unitlinks

import org.scalatest.{ FreeSpec, Matchers }
import uk.gov.ons.sbr.models.unitlinks.UnitType.LegalUnit

class UnitLinksQualifierSpec extends FreeSpec with Matchers {

  "A link" - {
    "to a parent" - {
      "has a column name comprised of the parent prefix and the parent unit type" in {
        UnitLinksQualifier.toParent(LegalUnit) shouldBe "p_LEU"
      }
    }
  }
}
