package repository.hbase

import org.scalatest.{ FreeSpec, Matchers }

class ColumnSpec extends FreeSpec with Matchers {

  "A fully qualified column name" - {
    "is comprised of" - {
      "a column family and a column family qualifier" in {
        Column.name(Column("family", "qualifier")) shouldBe "family:qualifier"
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
        Column.name(Column("family", "")) shouldBe "family:"
      }
    }
  }
}
