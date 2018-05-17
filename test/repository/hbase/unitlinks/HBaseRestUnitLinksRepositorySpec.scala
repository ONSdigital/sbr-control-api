package repository.hbase.unitlinks

import scala.concurrent.Future

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ EitherValues, FreeSpec, Matchers }

import uk.gov.ons.sbr.models.unitlinks.UnitLinks

import repository.RestRepository.Row
import repository.hbase.HBase.LinksColumnFamily
import repository.{ RestRepository, RowMapper }
import support.sample.SampleUnitLinks

class HBaseRestUnitLinksRepositorySpec extends FreeSpec with Matchers with MockFactory with ScalaFutures with EitherValues {

  private trait Fixture extends SampleUnitLinks {
    val TargetTable = "unit_links"
    val TargetRowKey = UnitLinksRowKey(SampleUnitId, SampleUnitType, SamplePeriod)

    val Fields = Map(s"c_$Enterprise" -> SampleEnterpriseParentId)
    val TargetUnitLinks = aUnitLinksSample(parents = Some(SampleParents), children = None)

    def toRow(fields: Map[String, String], rowkey: String = TargetRowKey): Row = Row(rowKey = rowkey, fields = fields)

    val restRepository = mock[RestRepository]
    val rowMapper = mock[RowMapper[UnitLinks]]
    val config = HBaseRestUnitLinksRepositoryConfig(TargetTable)
    val repository = new HBaseRestUnitLinksRepository(restRepository, config, rowMapper)

  }

  "A UnitLinks repository" - {
    "supports unit links retrieval when given unit id, unit type and period" - {
      "returning a target unit links when it exists" in new Fixture {
        (restRepository.findRow _).expects(TargetTable, TargetRowKey, LinksColumnFamily).returning(Future.successful(Right(Some(toRow(Fields)))))
        (rowMapper.fromRow _).expects(toRow(Fields)).returning(Some(TargetUnitLinks))

        whenReady(repository.retrieveUnitLinks(SampleUnitId, SampleUnitType, SamplePeriod)) { result =>
          result.right.value shouldBe Some(TargetUnitLinks)
        }
      }

      "returning nothing when unit links does not exist" in new Fixture {
        (restRepository.findRow _).expects(TargetTable, TargetRowKey, LinksColumnFamily).returning(Future.successful(Right(None)))

        whenReady(repository.retrieveUnitLinks(SampleUnitId, SampleUnitType, SamplePeriod)) { result =>
          result.right.value shouldBe None
        }
      }

      "signals a failure when a valid unit links is retrieve from HBase Rest but rowMapper fails to construct" in new Fixture {
        val fields = Map("x_UNKNOWN" -> SampleEnterpriseParentId)
        (restRepository.findRow _).expects(TargetTable, TargetRowKey, LinksColumnFamily).returning(Future.successful(Right(Some(toRow(fields)))))
        (rowMapper.fromRow _).expects(toRow(fields)).returning(None)

        whenReady(repository.retrieveUnitLinks(SampleUnitId, SampleUnitType, SamplePeriod)) { result =>
          result.left.value shouldBe "Unable to create Unit Links from row"
        }
      }

      "signals failure when failure occurs from underlying Rest repository" in new Fixture {
        (restRepository.findRow _).expects(TargetTable, TargetRowKey, LinksColumnFamily).returning(Future.successful(Left("A Failure Message")))

        whenReady(repository.retrieveUnitLinks(SampleUnitId, SampleUnitType, SamplePeriod)) { result =>
          result.left.value shouldBe "A Failure Message"

        }
      }
    }
  }

}
