package repository.hbase

import java.time.Month.JANUARY

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FreeSpec, Matchers, OptionValues }
import repository.hbase.HBase.DefaultColumnGroup
import repository.{ RestRepository, RowMapper }
import support.sample.SampleLocalUnit
import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.enterprise.Ern
import uk.gov.ons.sbr.models.localunit.{ LocalUnit, Lurn }

import scala.concurrent.Future

class HBaseRestLocalUnitRepositorySpec extends FreeSpec with Matchers with MockFactory with ScalaFutures with OptionValues {

  private trait Fixture extends SampleLocalUnit {
    val TargetErn = Ern("1000000013")
    val TargetPeriod = Period.fromYearMonth(2018, JANUARY)
    val TargetLurn = Lurn("900000015")
    val TargetLocalUnit = aLocalUnit(TargetErn, TargetLurn)
    val TargetRowKey = LocalUnitRowKey(TargetErn, TargetPeriod, TargetLurn)
    val SomeRow = Map("name" -> "value")
    val TargetTable = "local_unit"

    val restRepository = mock[RestRepository]
    val rowMapper = mock[RowMapper[LocalUnit]]
    val config = HBaseRestLocalUnitRepositoryConfig(TargetTable)
    val repository = new HBaseRestLocalUnitRepository(config, restRepository, rowMapper)
  }

  "A LocalUnit repository" - {
    "can retrieve a local unit by Enterprise reference (ERN), period, and Local Unit reference (LURN) when it exists" in new Fixture {
      (restRepository.get _).expects(TargetTable, TargetRowKey, DefaultColumnGroup).returning(Future.successful(Seq(SomeRow)))
      (rowMapper.fromRow _).expects(SomeRow).returning(Some(TargetLocalUnit))

      whenReady(repository.retrieveLocalUnit(TargetErn, TargetPeriod, TargetLurn)) { result =>
        result.value shouldBe TargetLocalUnit
      }
    }
  }
}
