package repository.hbase.enterprise

import javax.inject.Inject

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import com.typesafe.scalalogging.LazyLogging

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.{Enterprise, Ern}

import repository.RestRepository.{ErrorMessage, Row}
import repository.hbase.HBase.DefaultColumnFamily
import repository.{EnterpriseUnitRepository, RestRepository, RowMapper}

case class HBaseRestEnterpriseUnitRepositoryConfig(tableName: String)

class HBaseRestEnterpriseUnitRepository @Inject() (
    restRepository: RestRepository,
    config: HBaseRestEnterpriseUnitRepositoryConfig,
    rowMapper: RowMapper[Enterprise]
) extends EnterpriseUnitRepository with LazyLogging {

  override def retrieveEnterpriseUnit(ern: Ern, period: Period): Future[Either[ErrorMessage, Option[Enterprise]]] = {
    logger.info(s"Retrieving Enterprise with [$ern] for [$period].")
    restRepository.findRow(config.tableName, EnterpriseUnitRowKey(ern, period), DefaultColumnFamily).map(fromErrorOrRow)
  }

  private def fromErrorOrRow(errorOrRow: Either[ErrorMessage, Option[Row]]): Either[ErrorMessage, Option[Enterprise]] = {
    logger.debug(s"Enterprise Unit response is [$errorOrRow]")
    errorOrRow.right.flatMap { optRow =>
      optRow.map(fromRow).fold[Either[ErrorMessage, Option[Enterprise]]](Right(None)) { errorOrEnterprise =>
        logger.debug(s"From row to Enterprise Unit conversion result is [$errorOrEnterprise].")
        errorOrEnterprise.right.map(Some(_))
      }
    }
  }

  private def fromRow(row: Row): Either[ErrorMessage, Enterprise] = {
    val optEnterprise = rowMapper.fromRow(row)
    if (optEnterprise.isEmpty) logger.warn(s"Unable to construct a Enterprise Unit from HBase [${config.tableName}] row [$row].")
    optEnterprise.toRight("Unable to construct a Enterprise Unit from Row data")
  }
}
