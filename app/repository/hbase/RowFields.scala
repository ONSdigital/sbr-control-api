package repository.hbase

import repository.RestRepository.{ Field, RowKey }
import repository.hbase.HBaseData.{ HBaseCell, HBaseRow }

object RowFields {
  def apply(rowKey: RowKey, field: Field, otherFields: Seq[Field] = Seq.empty): Seq[HBaseRow] =
    Seq(HBaseRow(
      key = rowKey,
      cells = (field +: otherFields).map { f =>
        HBaseCell.fromField(f)
      }
    ))
}
