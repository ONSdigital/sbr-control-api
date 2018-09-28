package repository.hbase

import org.scalatest.{ FreeSpec, Matchers }
import repository.hbase.HBaseData.{ HBaseCell, HBaseRow }

class SingleFieldSpec extends FreeSpec with Matchers {

  private trait Fixture {
    val RowKey = "LEU~1234567890123456"
    val ColumnName = Column("family", "qualifier")
    val TargetValue = "abc"
  }

  "A single value specification" - {
    "defines the target cell value" in new Fixture {
      SingleField(rowKey = RowKey, field = ColumnName -> TargetValue) shouldBe Seq(
        HBaseRow(key = RowKey, cells = Seq(
          HBaseCell(column = "family:qualifier", value = TargetValue)
        ))
      )
    }
  }
}
