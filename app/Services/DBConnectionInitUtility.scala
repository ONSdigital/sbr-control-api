package Services

import scala.annotation.switch

import org.slf4j.LoggerFactory

import config.Properties.defaultDBInit
import config.SBRPropertiesConfiguration

/**
 * Created by haqa on 26/09/2017.
 */
object DBConnectionInitUtility {

  private[this] val logger = LoggerFactory.getLogger(SBRPropertiesConfiguration.getClass)

  def init(): DBConnector = (defaultDBInit: @switch) match {
    case s if s.equalsIgnoreCase("sql") =>
      logger.info(s"Starting SQL db service. Environment variable set to $defaultDBInit")
      new SQLConnect
    case s if s.equalsIgnoreCase("hbase") =>
      logger.info(s"Starting HBase db service. Environment variable set to $defaultDBInit")
      new HBaseConnect
    case _ =>
      logger.info(s"Environment variable with $defaultDBInit is invalid. Failed to continue operation.")
      sys.error(s"$defaultDBInit is not a valid DB option.")
  }


}
