package repository.hbase

import org.scalatest.{ FreeSpec, Matchers }
import repository.hbase.HBaseData.{ HBaseCell, HBaseRow }

class CheckAndUpdateSpec extends FreeSpec with Matchers {

  private trait Fixture {
    val RowKey = "VAT~123456789012"
    val Column = "family:name"
    val TargetValue = "abc"
    val CheckValue = "zyx"
  }

  "A check and update specification" - {
    "defines the target cell value followed by a check value (which is used to protect against 'lost updates')" in new Fixture {
      CheckAndUpdate(rowKey = RowKey, beforeField = Column -> CheckValue, afterField = Column -> TargetValue) shouldBe Seq(
        HBaseRow(key = RowKey, cells = Seq(
          HBaseCell(column = Column, value = TargetValue),
          HBaseCell(column = Column, value = CheckValue)
        ))
      )
    }
  }
}
