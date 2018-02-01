package services

import scala.annotation.switch

import org.slf4j.LoggerFactory
import play.api.Configuration

import config.Properties.defaultDBInit
import config.SBRPropertiesConfiguration

/**
 * Created by haqa on 26/09/2017.
 */

//@todo - change to object and use Config rather than play-Configuration
class DBConnectionInitUtility(playConfig: Configuration) {

  private[this] val logger = LoggerFactory.getLogger(SBRPropertiesConfiguration.getClass)

  //  def init(): DBConnector = (defaultDBInit: @switch) match {
  def init(): DBConnector = (playConfig.getString("db.default.name").getOrElse(defaultDBInit): @switch) match {
    case s if s.equalsIgnoreCase("hbase") =>
      logger.info(s"Starting HBase db service. Environment variable set to $defaultDBInit")
      new HBaseConnect
    case _ =>
      logger.info(s"Environment variable with $defaultDBInit is invalid. Failed to continue operation.")
      sys.error(s"$defaultDBInit is not a valid DB option.")
  }

}
