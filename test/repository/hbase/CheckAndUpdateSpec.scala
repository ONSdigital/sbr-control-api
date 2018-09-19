package repository.hbase

import org.scalatest.{ FreeSpec, Matchers }
import repository.hbase.HBaseData.{ HBaseCell, HBaseRow }

class CheckAndUpdateSpec extends FreeSpec with Matchers {

  private trait Fixture {
    val RowKey = "VAT~123456789012"
    val ColumnName = Column("family", "qualifier")
    val TargetValue = "abc"
    val CheckValue = "zyx"
  }

  "A check and update specification" - {
    "defines the target cell value followed by a check value (which is used to protect against 'lost updates')" in new Fixture {
      CheckAndUpdate(rowKey = RowKey, checkField = ColumnName -> CheckValue, updateField = ColumnName -> TargetValue) shouldBe Seq(
        HBaseRow(key = RowKey, cells = Seq(
          HBaseCell(column = "family:qualifier", value = TargetValue),
          HBaseCell(column = "family:qualifier", value = CheckValue)
        ))
      )
    }
  }
}
