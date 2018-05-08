package support

import uk.gov.ons.sbr.models.unitlinks.UnitType

import repository.hbase.HBase.{ unitChildPrefix, unitParentPrefix }

trait HBaseResponseFixture {
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

  def aColumnWith(name: String, value: String, timestamp: Long = 1520333985745L): String =
    s"""|{"column": "${hbaseEncode(name)}",
        | "timestamp": $timestamp,
        | "$$": "${hbaseEncode(value)}"
        |}""".stripMargin

  def aChildIdWithPrefix(id: String) = unitChildPrefix + id

  def aParentUnitTypeWithPrefix(unitType: UnitType) = unitParentPrefix + UnitType.toAcronym(unitType)
}
