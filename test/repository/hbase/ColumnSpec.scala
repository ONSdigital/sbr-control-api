package repository.hbase

import org.scalatest.{ FreeSpec, Matchers }

class ColumnSpec extends FreeSpec with Matchers {

  "A fully qualified column name" - {
    "is comprised of" - {
      "a column family and a column family qualifier" in {
        Column("family", "qualifier") shouldBe "family:qualifier"
      }

      "where the column family may not be" - {
        "empty" in {
          an[IllegalArgumentException] should be thrownBy Column("", "qualifier")
        }

        "blank" in {
          an[IllegalArgumentException] should be thrownBy Column("  ", "qualifier")
        }
      }

      "where the column family qualifier may be empty" in {
        Column("family", "") shouldBe "family:"
      }
    }

    "can be split into its constituent components (of column family and column family qualifier)" in {
      "family:qualifier" should matchPattern { case Column("family", "qualifier") => }
    }

    "is invalid" - {
      "when it does not contain the expected delimiter" in {
        Column.unapply("familyqualifier") shouldBe None
      }

      "when the column family is empty" in {
        Column.unapply(":qualifier") shouldBe None
      }

      "when the column family is blank" in {
        Column.unapply("  :qualifier") shouldBe None
      }
    }
  }
}
