package Services

import config.Properties.defaultDBInit
import config.SBRPropertiesConfiguration
import org.slf4j.LoggerFactory

/**
  * Created by haqa on 26/09/2017.
  */
object DBConnectionInitUtility {

  private[this] val logger = LoggerFactory.getLogger(SBRPropertiesConfiguration.getClass)

  def init: DBConnector = {
    defaultDBInit match {
      case s if s.equalsIgnoreCase("sql") =>
        logger.info(s"Starting SQL db service. Environment variable set to $defaultDBInit")
        new SQLConnect
      case s if s.equalsIgnoreCase("hbase") =>
        logger.info(s"Starting HBase db service. Environment variable set to $defaultDBInit")
        new HBaseConnect
      case _ => sys.error(s"$defaultDBInit is not a valid DB option.")
    }
  }


}
