package config

import com.typesafe.config.Config
import org.apache.hadoop.hbase.TableName
import play.api.Configuration

/**
 * Created by haqa on 28/07/2017.
 */
trait Properties {

  implicit val configuration: Configuration
  lazy private val propertiesConfig: Config = configuration.underlying

  lazy val dbConfig = propertiesConfig.getConfig("db")

  lazy val requestTimeout: Int = propertiesConfig.getInt("request.timeout")
  lazy val minKeyLength: Int = propertiesConfig.getInt("search.minKeyLength")

  private val hBaseConfig: Config = configuration.underlying.getConfig("db")
  private val hBaseRestConfig: Config = hBaseConfig.getConfig("hbase-rest")

  private val hbaseRestNameSpace: String = hBaseRestConfig.getString("namespace")

  lazy val enterpriseTableName: TableName = TableName.valueOf(
    hbaseRestNameSpace,
    hBaseRestConfig.getString("enterprise.table.name")
  )

  lazy val unitTableName: TableName = TableName.valueOf(
    hbaseRestNameSpace,
    hBaseRestConfig.getString("unit.links.table.name")
  )

  lazy val username: String = hBaseRestConfig.getString("username")
  lazy val password: String = hBaseRestConfig.getString("password")
  lazy val host: String = hBaseRestConfig.getString("host")
  lazy val port: String = hBaseRestConfig.getString("port")
  lazy val baseUrl: String = s"$host:$port"
  lazy val enterpriseColumnFamily: String = hBaseRestConfig.getString("column.family.enterprise")
  lazy val unitLinksColumnFamily: String = hBaseRestConfig.getString("column.family.unit.links")

}
