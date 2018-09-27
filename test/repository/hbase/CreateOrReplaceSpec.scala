package repository.hbase

import org.scalatest.{ FreeSpec, Matchers }
import repository.hbase.HBaseData.{ HBaseCell, HBaseRow }

class CreateOrReplaceSpec extends FreeSpec with Matchers {

  private trait Fixture {
    val RowKey = "LEU~1234567890123456"
    val Column = "family:name"
    val TargetValue = "abc"
  }

  "A create or replace field specification" - {
    "defines the target cell value" in new Fixture {
      CreateOrReplace(rowKey = RowKey, field = Column -> TargetValue) shouldBe Seq(
        HBaseRow(key = RowKey, cells = Seq(
          HBaseCell(column = Column, value = TargetValue)
        ))
      )
    }
  }
}
