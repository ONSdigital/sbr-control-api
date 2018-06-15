package repository.hbase

object HBase {
  val RowKeyDelimiter = "~"
  val Wildcard = "*"
  val DefaultColumnFamily = "d"

  def rowKeyUrl(protocol: String, hostname: String, port: Int, prefix: Option[String], namespace: String, table: String, rowKey: String, columnFamily: String): String =
    s"$protocol://$hostname:$port/${prefix.fold("")(p => if (p.trim.isEmpty()) "" else p + "/")}${rowKeyUrl(namespace, table, rowKey, columnFamily)}"

  def rowKeyUrl(namespace: String, table: String, rowKey: String, columnFamily: String): String =
    s"$namespace:$table/$rowKey/$columnFamily"
}
