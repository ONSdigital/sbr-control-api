package repository.hbase

import repository.RestRepository.{ Field, RowKey }
import repository.hbase.HBaseData.{ HBaseCell, HBaseRow }

private[hbase] object CheckAndUpdate {
  /*
   * The order of the cells is important - "check value" must be last.
   */
  def apply(rowKey: RowKey, checkField: Field, updateField: Field, otherUpdateFields: Seq[Field] = Seq.empty): Seq[HBaseRow] =
    Seq(HBaseRow(key = rowKey, cells = (updateField +: otherUpdateFields :+ checkField).map { f =>
      HBaseCell.fromField(f)
    }))
}
