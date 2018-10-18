package repository.hbase

import org.scalatest.{ FreeSpec, Matchers }
import repository.hbase.HBaseData.{ HBaseCell, HBaseRow }

class CheckAndUpdateSpec extends FreeSpec with Matchers {

  private trait Fixture {
    val RowKey = "VAT~123456789012"
    val ColumnName = Column("family", "qualifier")
    val TargetValue = "abc"
    val CheckValue = "zyx"
    val EditedFlag = Column("family", "edited")
    val EditedValue = "Y"
  }

  "A check and update specification" - {
    "defines the target cell values before a check value (which is used to protect against 'lost updates')" - {
      "when a single field is being updated" in new Fixture {
        CheckAndUpdate(rowKey = RowKey, checkField = ColumnName -> CheckValue, updateField = ColumnName -> TargetValue) shouldBe Seq(
          HBaseRow(key = RowKey, cells = Seq(
            HBaseCell(column = "family:qualifier", value = TargetValue),
            HBaseCell(column = "family:qualifier", value = CheckValue)
          ))
        )
      }

      "when one field is being updated and another field is being created" in new Fixture {
        CheckAndUpdate(rowKey = RowKey, checkField = ColumnName -> CheckValue,
          updateField = ColumnName -> TargetValue, otherUpdateFields = Seq(EditedFlag -> EditedValue)) shouldBe Seq(
          HBaseRow(key = RowKey, cells = Seq(
            HBaseCell(column = "family:qualifier", value = TargetValue),
            HBaseCell(column = "family:edited", value = EditedValue),
            HBaseCell(column = "family:qualifier", value = CheckValue)
          ))
        )
      }
    }
  }
}
