package repository.hbase.unit.enterprise
import javax.inject.Inject

import scala.concurrent.Future

import com.typesafe.scalalogging.LazyLogging

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.{ Enterprise, Ern }

import repository.hbase.HBase.DefaultColumnGroup
import repository.{ RestRepository, RowMapper }
import scala.concurrent.ExecutionContext.Implicits.global

case class HBaseRestEnterpriseUnitRepositoryConfig(tableName: String)

class HBaseRestEnterpriseUnitRepository @Inject() (
    respRepository: RestRepository,
    config: HBaseRestEnterpriseUnitRepositoryConfig,
    rowMapper: RowMapper[Enterprise]
) extends EnterpriseUnitRepository with LazyLogging {

  override def retrieveEnterpriseUnit(ern: Ern, period: Period): Future[Option[Enterprise]] = {
    respRepository.get(table = config.tableName, rowKey = EnterpriseUnitRowKey(ern, period), columnGroup = DefaultColumnGroup).map { row =>
      logger.info(s"Retrieving Local Unit with [$ern] for [$period].")
      val resOpt = row.headOption.flatMap(rowMapper.fromRow)
      logger.debug(s"Local Unit result was [$resOpt].")
      resOpt
    }
  }

}
