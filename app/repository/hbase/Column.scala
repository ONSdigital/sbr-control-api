package repository.hbase

/**
 * A HBase column identifier comprises a column family together with a qualifier.
 *
 * @param family the column family
 * @param qualifier the column qualifier
 * @throws IllegalArgumentException if family is blank
 */
case class Column(family: String, qualifier: String) {
  require(family.trim.nonEmpty, "Column family cannot be blank")
}

object Column {
  private val Delimiter = ":"

  def name(column: Column): String =
    column.family + Delimiter + column.qualifier
}
