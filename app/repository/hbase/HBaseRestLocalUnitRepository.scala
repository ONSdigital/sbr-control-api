package repository.hbase

import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import repository.hbase.HBase.DefaultColumnGroup
import repository.{ LocalUnitRepository, RestRepository, RowMapper }
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.{ LocalUnit, Lurn }

import scala.concurrent.Future

case class HBaseRestLocalUnitRepositoryConfig(tableName: String)

class HBaseRestLocalUnitRepository @Inject() (
    config: HBaseRestLocalUnitRepositoryConfig,
    restRepository: RestRepository,
    rowMapper: RowMapper[LocalUnit]
) extends LocalUnitRepository with LazyLogging {
  override def retrieveLocalUnit(ern: Ern, period: Period, lurn: Lurn): Future[Option[LocalUnit]] = {
    logger.info(s"Retrieving Local Unit with [$ern] [$lurn] for [$period].")
    restRepository.get(table = config.tableName, rowKey = LocalUnitRowKey(ern, period, lurn), columnGroup = DefaultColumnGroup).map { rows =>
      val resultOpt = rows.headOption.flatMap(rowMapper.fromRow)
      logger.debug(s"Local Unit result was [$resultOpt].")
      resultOpt
    }
  }
}
