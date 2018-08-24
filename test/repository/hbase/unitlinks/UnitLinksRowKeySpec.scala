package repository.hbase.unitlinks

import org.scalatest.{ FreeSpec, Matchers }
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitType }

class UnitLinksRowKeySpec extends FreeSpec with Matchers {

  private trait Fixture {
    val Id = "109085670091"
    val Type = "LEU"
    val RowKey = s"$Type~$Id"
  }

  "A Unit Links Row Key" - {
    "is comprised of the unit type and the unit identifier" in new Fixture {
      UnitLinksRowKey(UnitId(Id), UnitType.LegalUnit) shouldBe RowKey
    }

    "can be exploded into its individual components when valid" in new Fixture {
      RowKey should matchPattern { case UnitLinksRowKey(UnitId(Id), UnitType.LegalUnit) => }
    }

    "cannot be exploded into its individual components when invalid due to" - {
      "an incorrect number of components" - {
        "when too many" in new Fixture {
          val badRowKey = s"$RowKey~201801"

          UnitLinksRowKey.unapply(badRowKey) shouldBe None
        }

        "when too few" in new Fixture {
          val badRowKey = Id

          UnitLinksRowKey.unapply(badRowKey) shouldBe None
        }
      }

      "an unrecognised UnitType" in new Fixture {
        val badRowKey = s"UNKNOWN~$Id"

        UnitLinksRowKey.unapply(badRowKey) shouldBe None
      }
    }
  }
}
