package repository.hbase

import utils.BaseUrl

object HBase {
  val RowKeyDelimiter = "~"
  val Wildcard = "*"
  val DefaultColumnFamily = "d"

  /*
   * Note that the HBase REST documentation at https://hbase.apache.org/book.html#_rest does not describe a GET
   * endpoint that specifies the columnFamily.  However, our experience with Cloudera is that this is required
   * when performing prefix searches (ie. those ending in the wildcard).  For simplicity, we consistently use
   * this endpoint for GET requests (ie. even when the query is for a specific rowKey and does not contain the
   * wildcard).
   */
  def rowKeyColFamilyUrl(withBase: BaseUrl, namespace: String, table: String, rowKey: String, columnFamily: String): String =
    s"${BaseUrl.asUrlString(withBase)}/${rowKeyColFamilyUrl(namespace, table, rowKey, columnFamily)}"

  def rowKeyColFamilyUrl(namespace: String, table: String, rowKey: String, columnFamily: String): String =
    s"${rowKeyUrl(namespace, table, rowKey)}/$columnFamily"

  /*
   * ColumnFamily should not be specified in the URL when writing to HBase (each individual cell specifies the family
   * of its target column in that case).
   */
  def checkedPutUrl(withBase: BaseUrl, namespace: String, table: String, rowKey: String): String =
    s"${BaseUrl.asUrlString(withBase)}/${checkedPutUrl(namespace, table, rowKey)}"

  def checkedPutUrl(namespace: String, table: String, rowKey: String): String =
    s"${rowKeyUrl(namespace, table, rowKey)}/?check=put"

  def rowKeyUrl(withBase: BaseUrl, namespace: String, table: String, rowKey: String): String =
    s"${BaseUrl.asUrlString(withBase)}/${rowKeyUrl(namespace, table, rowKey)}"

  def rowKeyUrl(namespace: String, table: String, rowKey: String): String =
    s"$namespace:$table/$rowKey"
}
