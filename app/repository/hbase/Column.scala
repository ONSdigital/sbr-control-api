package repository.hbase

import scala.reflect.internal.util.StringOps

object Column {
  private val Delimiter = ":"

  def apply(family: String, qualifier: String): String =
    if (family.trim.isEmpty) throw new IllegalArgumentException("Column family cannot be blank")
    else family + Delimiter + qualifier

  def unapply(column: String): Option[(String, String)] =
    splitColumn(column).flatMap {
      case (family, _) if family.trim.isEmpty => None
      case col => Some(col)
    }

  private def splitColumn(column: String): Option[(String, String)] = {
    val delimiterIndex = column.indexOf(Delimiter)
    if (delimiterIndex < 0) None else StringOps.splitAt(column, delimiterIndex, doDropIndex = true)
  }
}
