package repository.hbase

import org.scalatest.{ FreeSpec, Matchers }
import repository.hbase.HBaseData.{ HBaseCell, HBaseRow }

class RowFieldsSpec extends FreeSpec with Matchers {

  private trait Fixture {
    val RowKey = "LEU~1234567890123456"
    val ColumnName = Column("family", "qualifier")
    val TargetValue = "abc"
    val OtherColumnName = Column("otherFamily", "otherQualifier")
    val OtherTargetValue = "xyz"
  }

  "A row fields specification" - {
    "can define a single cell" in new Fixture {
      RowFields(rowKey = RowKey, field = ColumnName -> TargetValue) shouldBe Seq(
        HBaseRow(key = RowKey, cells = Seq(
          HBaseCell(column = "family:qualifier", value = TargetValue)
        ))
      )
    }

    "can define multiple cells" in new Fixture {
      RowFields(rowKey = RowKey, field = ColumnName -> TargetValue, otherFields = Seq(OtherColumnName -> OtherTargetValue)) shouldBe Seq(
        HBaseRow(key = RowKey, cells = Seq(
          HBaseCell(column = "family:qualifier", value = TargetValue),
          HBaseCell(column = "otherFamily:otherQualifier", value = OtherTargetValue)
        ))
      )
    }
  }
}
