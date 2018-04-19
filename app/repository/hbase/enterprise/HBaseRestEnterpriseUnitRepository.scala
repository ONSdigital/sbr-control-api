package repository.hbase.enterprise

import javax.inject.Inject

import scala.concurrent.Future

import com.typesafe.scalalogging.LazyLogging

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern }

import repository.hbase.HBase.DefaultColumnGroup
import repository.{ EnterpriseUnitRepository, RestRepository, RowMapper }
import play.api.libs.concurrent.Execution.Implicits.defaultContext

case class HBaseRestEnterpriseUnitRepositoryConfig(tableName: String)

class HBaseRestEnterpriseUnitRepository @Inject() (
    restRepository: RestRepository,
    config: HBaseRestEnterpriseUnitRepositoryConfig,
    rowMapper: RowMapper[Enterprise]
) extends EnterpriseUnitRepository with LazyLogging {

  override def retrieveEnterpriseUnit(ern: Ern, period: Period): Future[Option[Enterprise]] = {
    logger.info(s"Retrieving Enterprise with [$ern] for [$period].")
    restRepository.findRow(config.tableName, EnterpriseUnitRowKey(ern, period), DefaultColumnGroup).map { errorOrRow =>
      errorOrRow.fold(
        _ => None,
        optRow => {
          val optEnterprise = optRow.flatMap(rowMapper.fromRow)
          logger.debug(s"Enterprise result was [$optEnterprise].")
          optEnterprise
        }
      )
    }
  }
}
