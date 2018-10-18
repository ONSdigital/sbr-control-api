package repository.hbase.unitlinks

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ EitherValues, FreeSpec, Matchers }
import repository.RestRepository.Row
import repository._
import repository.hbase.Column
import repository.hbase.unitlinks.HBaseRestUnitLinksRepository.ColumnFamily
import services.{ UnitFound, UnitNotFound, UnitRegisterFailure, UnitRegisterService }
import support.sample.SampleUnitLinks
import uk.gov.ons.sbr.models.unitlinks.UnitType.toAcronym
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitLinks, UnitLinksNoPeriod, UnitType }
import uk.gov.ons.sbr.models.{ Period, UnitKey }

import scala.concurrent.Future

class HBaseRestUnitLinksRepositorySpec extends FreeSpec with Matchers with MockFactory with ScalaFutures with EitherValues {

  private trait Fixture extends SampleUnitLinks {
    val TargetBaseTable = "unit_link"
    val TargetPeriodTable = s"${TargetBaseTable}_${Period.asString(SamplePeriod)}"
    val TargetRowKey = UnitLinksRowKey(SampleUnitId, SampleUnitType)
    val DummyFields = Map(s"c_$Enterprise" -> SampleEnterpriseParentId)

    val TargetUnitLinkKey = UnitKey(SampleUnitId, SampleUnitType, SamplePeriod)
    val TargetUnitLinksNoPeriod = SampleUnitLinksNoPeriodWithAllFields.copy(children = None)
    val TargetUnitLinks = UnitLinks(
      id = TargetUnitLinksNoPeriod.id,
      period = SamplePeriod,
      parents = TargetUnitLinksNoPeriod.parents,
      children = TargetUnitLinksNoPeriod.children,
      unitType = TargetUnitLinksNoPeriod.unitType
    )

    def toRow(fields: Map[String, String], rowkey: String = TargetRowKey): Row =
      Row(rowKey = rowkey, fields = fields)

    val restRepository = mock[RestRepository]
    val rowMapper = mock[RowMapper[UnitLinksNoPeriod]]
    val config = HBaseRestUnitLinksRepositoryConfig(TargetBaseTable)
    val unitRegisterService = mock[UnitRegisterService]
    val repository = new HBaseRestUnitLinksRepository(restRepository, config, rowMapper, unitRegisterService)
  }

  private trait EditFixture extends Fixture {
    val EditedColumnName = Column(ColumnFamily, "edited")
    val EditedValue = "Y"
  }

  private trait EditParentFixture extends EditFixture {
    val IncorrectLegalUnitId = UnitId("1230000000000100")
    val TargetLegalUnitId = UnitId("1230000000000200")
    val ColumnName = Column(ColumnFamily, "p_LEU")
    val UpdateDescriptor = UpdateParentDescriptor(
      parentType = UnitType.LegalUnit,
      fromParentId = IncorrectLegalUnitId, toParentId = TargetLegalUnitId
    )
  }

  private trait EditChildFixture extends EditFixture {
    val ChildUnitType = UnitType.ValueAddedTax
    val ChildUnitId = UnitId("987654321012")
    val ColumnName = Column(ColumnFamily, s"c_${ChildUnitId.value}")
  }

  "A UnitLinks repository" - {
    "supports unit links retrieval when given unit id, unit type and period" - {
      "returning a target unit links when it exists" in new Fixture {
        (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, ColumnFamily).returning(
          Future.successful(Right(Some(toRow(DummyFields))))
        )
        (rowMapper.fromRow _).expects(toRow(DummyFields)).returning(Some(TargetUnitLinksNoPeriod))

        whenReady(repository.retrieveUnitLinks(TargetUnitLinkKey)) { result =>
          result.right.value shouldBe Some(TargetUnitLinks)
        }
      }

      "returning nothing when unit links does not exist" in new Fixture {
        (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, ColumnFamily).returning(
          Future.successful(Right(None))
        )

        whenReady(repository.retrieveUnitLinks(TargetUnitLinkKey)) { result =>
          result.right.value shouldBe None
        }
      }

      "signals a failure when the row mapper is unable to construct a valid UnitLinks representation from the HBase row" in new Fixture {
        (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, ColumnFamily).returning(
          Future.successful(Right(Some(toRow(DummyFields))))
        )
        (rowMapper.fromRow _).expects(toRow(DummyFields)).returning(None)

        whenReady(repository.retrieveUnitLinks(TargetUnitLinkKey)) { result =>
          result.left.value shouldBe "Unable to create Unit Links from row"
        }
      }

      "signals failure when failure occurs from underlying Rest repository" in new Fixture {
        (restRepository.findRow _).expects(TargetPeriodTable, TargetRowKey, ColumnFamily).returning(
          Future.successful(Left("A Failure Message"))
        )

        whenReady(repository.retrieveUnitLinks(TargetUnitLinkKey)) { result =>
          result.left.value shouldBe "A Failure Message"
        }
      }
    }

    "supports the update of a parent link (setting the clerically edited flag)" - {
      "returning EditApplied when successful" in new EditParentFixture {
        (restRepository.updateField _).expects(TargetPeriodTable, TargetRowKey,
          (ColumnName, IncorrectLegalUnitId.value), (ColumnName, TargetLegalUnitId.value), Seq(EditedColumnName -> EditedValue)).returning(
            Future.successful(EditApplied)
          )

        whenReady(repository.updateParentLink(TargetUnitLinkKey, UpdateDescriptor)) { result =>
          result shouldBe EditApplied
        }
      }

      "returning EditConflicted when target link has been modified by another user" in new EditParentFixture {
        (restRepository.updateField _).expects(TargetPeriodTable, TargetRowKey,
          (ColumnName, IncorrectLegalUnitId.value), (ColumnName, TargetLegalUnitId.value), Seq(EditedColumnName -> EditedValue)).returning(
            Future.successful(EditConflicted)
          )

        whenReady(repository.updateParentLink(TargetUnitLinkKey, UpdateDescriptor)) { result =>
          result shouldBe EditConflicted
        }
      }

      "returning EditTargetNotFound when the target link does not exist" in new EditParentFixture {
        (restRepository.updateField _).expects(TargetPeriodTable, TargetRowKey,
          (ColumnName, IncorrectLegalUnitId.value), (ColumnName, TargetLegalUnitId.value), Seq(EditedColumnName -> EditedValue)).returning(
            Future.successful(EditTargetNotFound)
          )

        whenReady(repository.updateParentLink(TargetUnitLinkKey, UpdateDescriptor)) { result =>
          result shouldBe EditTargetNotFound
        }
      }

      "returning EditFailed when the update is attempted but is unsuccessful" in new EditParentFixture {
        (restRepository.updateField _).expects(TargetPeriodTable, TargetRowKey,
          (ColumnName, IncorrectLegalUnitId.value), (ColumnName, TargetLegalUnitId.value), Seq(EditedColumnName -> EditedValue)).returning(
            Future.successful(EditFailed)
          )

        whenReady(repository.updateParentLink(TargetUnitLinkKey, UpdateDescriptor)) { result =>
          result shouldBe EditFailed
        }
      }
    }

    "supports the creation of a child link (setting the clerically edited flag)" - {
      "returning success when the target unit is known to the register and the create operation succeeds" in new EditChildFixture {
        (unitRegisterService.isRegisteredUnit _).expects(TargetUnitLinkKey).returning(
          Future.successful(UnitFound)
        )
        (restRepository.createOrReplace _).expects(TargetPeriodTable, TargetRowKey,
          (ColumnName, toAcronym(ChildUnitType)), Seq(EditedColumnName -> EditedValue)).returning(
            Future.successful(EditApplied)
          )

        whenReady(repository.createChildLink(TargetUnitLinkKey, ChildUnitType, ChildUnitId)) { result =>
          result shouldBe CreateChildLinkSuccess
        }
      }

      "returning not found when the target unit does not have any links in the register" in new EditChildFixture {
        (unitRegisterService.isRegisteredUnit _).expects(TargetUnitLinkKey).returning(
          Future.successful(UnitNotFound)
        )

        whenReady(repository.createChildLink(TargetUnitLinkKey, ChildUnitType, ChildUnitId)) { result =>
          result shouldBe LinkFromUnitNotFound
        }
      }

      "returning failure when the attempt to identity whether the target unit has links in the register fails" in new EditChildFixture {
        (unitRegisterService.isRegisteredUnit _).expects(TargetUnitLinkKey).returning(
          Future.successful(UnitRegisterFailure("lookup failed"))
        )

        whenReady(repository.createChildLink(TargetUnitLinkKey, ChildUnitType, ChildUnitId)) { result =>
          result shouldBe CreateChildLinkFailure
        }
      }

      "returning failure when an attempt to create the value fails" in new EditChildFixture {
        (unitRegisterService.isRegisteredUnit _).expects(TargetUnitLinkKey).returning(
          Future.successful(UnitFound)
        )
        (restRepository.createOrReplace _).expects(TargetPeriodTable, TargetRowKey,
          (ColumnName, toAcronym(ChildUnitType)), Seq(EditedColumnName -> EditedValue)).returning(
            Future.successful(EditFailed)
          )

        whenReady(repository.createChildLink(TargetUnitLinkKey, ChildUnitType, ChildUnitId)) { result =>
          result shouldBe CreateChildLinkFailure
        }
      }
    }

    "supports the deletion of a child link (setting the clerically edited flag)" - {
      "returning success when the operation is successful" in new EditChildFixture {
        (restRepository.deleteField _).expects(TargetPeriodTable, TargetRowKey, (ColumnName, toAcronym(ChildUnitType)), ColumnName).returning(
          Future.successful(EditApplied)
        )
        (restRepository.createOrReplace _).expects(TargetPeriodTable, TargetRowKey, EditedColumnName -> EditedValue, Seq()).returning(
          Future.successful(EditApplied)
        )

        whenReady(repository.deleteChildLink(TargetUnitLinkKey, ChildUnitType, ChildUnitId)) { result =>
          result shouldBe EditApplied
        }
      }

      "returning conflict when the target link has been modified by another user" in new EditChildFixture {
        (restRepository.deleteField _).expects(TargetPeriodTable, TargetRowKey, (ColumnName, toAcronym(ChildUnitType)), ColumnName).returning(
          Future.successful(EditConflicted)
        )

        whenReady(repository.deleteChildLink(TargetUnitLinkKey, ChildUnitType, ChildUnitId)) { result =>
          result shouldBe EditConflicted
        }
      }

      /*
       * We are deleting a cell not a row.
       * It is OK for the target cell not to exist, but the 'resource' (in this case the row) must exist.
       */
      "returning not found when the target unit does not exist " in new EditChildFixture {
        (restRepository.deleteField _).expects(TargetPeriodTable, TargetRowKey, (ColumnName, toAcronym(ChildUnitType)), ColumnName).returning(
          Future.successful(EditTargetNotFound)
        )

        whenReady(repository.deleteChildLink(TargetUnitLinkKey, ChildUnitType, ChildUnitId)) { result =>
          result shouldBe EditTargetNotFound
        }
      }

      "returning failure when an attempt to delete the value fails" in new EditChildFixture {
        (restRepository.deleteField _).expects(TargetPeriodTable, TargetRowKey, (ColumnName, toAcronym(ChildUnitType)), ColumnName).returning(
          Future.successful(EditFailed)
        )

        whenReady(repository.deleteChildLink(TargetUnitLinkKey, ChildUnitType, ChildUnitId)) { result =>
          result shouldBe EditFailed
        }
      }

      "returning failure when the value is successfully deleted but the attempt to set the edited flag fails" in new EditChildFixture {
        (restRepository.deleteField _).expects(TargetPeriodTable, TargetRowKey, (ColumnName, toAcronym(ChildUnitType)), ColumnName).returning(
          Future.successful(EditApplied)
        )
        (restRepository.createOrReplace _).expects(TargetPeriodTable, TargetRowKey, EditedColumnName -> EditedValue, Seq()).returning(
          Future.successful(EditFailed)
        )

        whenReady(repository.deleteChildLink(TargetUnitLinkKey, ChildUnitType, ChildUnitId)) { result =>
          result shouldBe EditFailed
        }
      }
    }
  }
}
