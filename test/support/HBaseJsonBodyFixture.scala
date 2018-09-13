package support

import repository.hbase.Column

trait HBaseJsonBodyFixture {
  /*
   * Cloudera currently returns a 200 OK Response containing an "empty row".
   * Note that the latest version of Apache HBase returns a 404 NOT FOUND Response -
   * so we may encounter a change in behaviour with a future Cloudera upgrade.
   */
  val NoMatchFoundResponse = """{"Row":[]}"""

  val hbaseEncode: String => String =
    Base64.encode

  def aRowWith(key: String, columns: String*): String =
    s"""|{"key": "${hbaseEncode(key)}",
        | "Cell": ${columns.mkString("[", ",", "]")}
        |}""".stripMargin

  /*
   * A timestamp will exist on a read, but not on a write.
   */
  def aColumnWith(family: String, qualifier: String, value: String, timestamp: Option[Long] = Some(1520333985745L)): String = {
    val name = Column(family, qualifier)
    timestamp.fold(
      s"""|{"column": "${hbaseEncode(name)}",
          | "$$": "${hbaseEncode(value)}"
          |}""".stripMargin
    ) { timestampValue =>
      s"""|{"column": "${hbaseEncode(name)}",
          | "timestamp": $timestampValue,
          | "$$": "${hbaseEncode(value)}"
          |}""".stripMargin
    }
  }
}
