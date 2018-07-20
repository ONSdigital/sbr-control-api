package config

import com.typesafe.config.Config
import org.apache.hadoop.hbase.TableName
import play.api.Configuration

trait Properties {

  implicit val configuration: Configuration
  lazy private val propertiesConfig: Config = configuration.underlying

  lazy val dbConfig = propertiesConfig.getConfig("db")

  // HBase REST
  private val hBaseRestConfig: Config = dbConfig.getConfig("hbase-rest")
  private val hbaseRestNameSpace: String = hBaseRestConfig.getString("namespace")
  lazy val timeout: Int = hBaseRestConfig.getInt("timeout")

  // HBase REST table/namespace/column family details
  lazy val enterpriseTableName: TableName = TableName.valueOf(
    hbaseRestNameSpace,
    hBaseRestConfig.getString("enterprise.table.name")
  )

  lazy val unitTableName: TableName = TableName.valueOf(
    hbaseRestNameSpace,
    hBaseRestConfig.getString("unit.links.table.name")
  )

  lazy val enterpriseColumnFamily: String = hBaseRestConfig.getString("column.family.enterprise")
  lazy val unitLinksColumnFamily: String = hBaseRestConfig.getString("column.family.unit.links")

  // HBase REST Auth/URLs
  lazy val username: String = hBaseRestConfig.getString("username")
  lazy val password: String = hBaseRestConfig.getString("password")
  lazy val protocol: String = hBaseRestConfig.getString("protocol")
  lazy val host: String = hBaseRestConfig.getString("host")
  lazy val port: String = hBaseRestConfig.getString("port")
  lazy val baseUrl: String = s"$protocol://$host:$port"

  // HBase REST data formatting config
  lazy val delimiter: String = hBaseRestConfig.getString("delimiter")
  lazy val columnFamilyAndValueSubstring: Int = 2

  // Units
  lazy val entUnit: String = "ENT"
  lazy val leuUnit: String = "LEU"
}
