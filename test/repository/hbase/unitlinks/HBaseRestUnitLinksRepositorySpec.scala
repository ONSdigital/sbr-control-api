package repository.hbase.unitlinks

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ EitherValues, FreeSpec, Matchers }
import repository.RestRepository.Row
import repository.hbase.unitlinks.HBaseRestUnitLinksRepository.ColumnFamily
import repository.{ RestRepository, RowMapper }
import support.sample.SampleUnitLinks
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{ UnitLinks, UnitLinksNoPeriod }

import scala.concurrent.Future

class HBaseRestUnitLinksRepositorySpec extends FreeSpec with Matchers with MockFactory with ScalaFutures with EitherValues {

  private trait Fixture extends SampleUnitLinks {
    val TargetBaseTable = "unit_links"
    val TargetPeriodTable = s"${TargetBaseTable}_${Period.asString(SamplePeriod)}"
    val TargetRowKey = UnitLinksRowKey(SampleUnitId, SampleUnitType)
    val DummyFields = Map(s"c_$Enterprise" -> SampleEnterpriseParentId)

    val TargetUnitLinksNoPeriod = SampleUnitLinksNoPeriodWithAllFields.copy(children = None)
    val TargetUnitLinks = UnitLinks(
      id = TargetUnitLinksNoPeriod.id,
      period = SamplePeriod,
      parents = TargetUnitLinksNoPeriod.parents,
      children = TargetUnitLinksNoPeriod.children,
      unitType = TargetUnitLinksNoPeriod.unitType
    )

    def toRow(fields: Map[String, String], rowkey: String = TargetRowKey): Row = Row(rowKey = rowkey, fields = fields)

    val restRepository = mock[RestRepository]
    val rowMapper = mock[RowMapper[UnitLinksNoPeriod]]
    val config = HBaseRestUnitLinksRepositoryConfig(TargetBaseTable)
    val repository = new HBaseRestUnitLinksRepository(restRepository, config, rowMapper)
  }

  "A UnitLinks repository" - {
    "supports unit links retrieval when given unit id, unit type and period" - {
      "returning a target unit links when it exists" in new Fixture {
        (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, ColumnFamily).returning(Future.successful(Right(Some(toRow(DummyFields)))))
        (rowMapper.fromRow _).expects(toRow(DummyFields)).returning(Some(TargetUnitLinksNoPeriod))

        whenReady(repository.retrieveUnitLinks(SampleUnitId, SampleUnitType, SamplePeriod)) { result =>
          result.right.value shouldBe Some(TargetUnitLinks)
        }
      }

      "returning nothing when unit links does not exist" in new Fixture {
        (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, ColumnFamily).returning(Future.successful(Right(None)))

        whenReady(repository.retrieveUnitLinks(SampleUnitId, SampleUnitType, SamplePeriod)) { result =>
          result.right.value shouldBe None
        }
      }

      "signals a failure when the row mapper is unable to construct a valid UnitLinks representation from the HBase row" in new Fixture {
        (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, ColumnFamily).returning(Future.successful(Right(Some(toRow(DummyFields)))))
        (rowMapper.fromRow _).expects(toRow(DummyFields)).returning(None)

        whenReady(repository.retrieveUnitLinks(SampleUnitId, SampleUnitType, SamplePeriod)) { result =>
          result.left.value shouldBe "Unable to create Unit Links from row"
        }
      }

      "signals failure when failure occurs from underlying Rest repository" in new Fixture {
        (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, ColumnFamily).returning(Future.successful(Left("A Failure Message")))

        whenReady(repository.retrieveUnitLinks(SampleUnitId, SampleUnitType, SamplePeriod)) { result =>
          result.left.value shouldBe "A Failure Message"
        }
      }
    }
  }
}
