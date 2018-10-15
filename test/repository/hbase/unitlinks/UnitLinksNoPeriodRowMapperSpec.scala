package repository.hbase.unitlinks

import org.scalatest.{ FreeSpec, Matchers }
import repository.RestRepository.Row
import support.sample.SampleUnitLinks
import uk.gov.ons.sbr.models.unitlinks.UnitType
import repository.hbase.unitlinks.UnitLinksQualifier.{ ChildPrefix, ParentPrefix }

class UnitLinksNoPeriodRowMapperSpec extends FreeSpec with Matchers {

  private trait Fixture extends SampleUnitLinks {
    val UnitIdStr = SampleUnitId.value
    val UnitTypeStr = UnitType.toAcronym(SampleUnitType)
    val RowKey = s"$UnitTypeStr~$UnitIdStr"

    val ParentsMap = Map(
      ParentPrefix + Enterprise -> SampleEnterpriseParentId
    )
    val ChildrenMap = Map(
      ChildPrefix + SampleCompaniesHouseChildId -> CompaniesHouse,
      ChildPrefix + SamplePayAsYouEarnChildId -> PayAsYouEarnTax
    )
    val FamilyMap = ParentsMap ++ ChildrenMap
    val EditedMap = Map("edited" -> "Y")
  }

  "A UnitLinksNoPeriodRowMapper" - {
    "can create a UnitLinksNoPeriod from a row" - {
      "when all fields define either parents or children" in new Fixture {
        val row = Row(RowKey, fields = FamilyMap)

        UnitLinksNoPeriodRowMapper.fromRow(row) shouldBe Some(SampleUnitLinksNoPeriodWithAllFields)
      }

      "ignoring fields that do not define parents or children" in new Fixture {
        val row = Row(RowKey, fields = FamilyMap ++ EditedMap)

        UnitLinksNoPeriodRowMapper.fromRow(row) shouldBe Some(SampleUnitLinksNoPeriodWithAllFields)
      }

      "when there are no children" in new Fixture {
        val row = Row(RowKey, fields = ParentsMap)

        UnitLinksNoPeriodRowMapper.fromRow(row) shouldBe Some(SampleUnitLinksNoPeriodWithAllFields.copy(children = None))
      }

      "when there are no parents" in new Fixture {
        val row = Row(RowKey, fields = ChildrenMap)

        UnitLinksNoPeriodRowMapper.fromRow(row) shouldBe Some(SampleUnitLinksNoPeriodWithAllFields.copy(parents = None))
      }

      "when there are neither parents or children" in new Fixture {
        val row = Row(RowKey, fields = Map.empty)

        UnitLinksNoPeriodRowMapper.fromRow(row) shouldBe Some(SampleUnitLinksNoPeriodWithAllFields.copy(parents = None, children = None))
      }
    }

    "cannot create a UnitLinksNoPeriod from a row" - {
      "when the row key is invalid" - {
        "because it contains too few elements" in new Fixture {
          val row = Row(rowKey = UnitIdStr, fields = FamilyMap)

          UnitLinksNoPeriodRowMapper.fromRow(row) shouldBe None
        }

        "because it contains too many elements" in new Fixture {
          val row = Row(rowKey = s"$RowKey~201801", fields = FamilyMap)

          UnitLinksNoPeriodRowMapper.fromRow(row) shouldBe None
        }

        "because it contains an unrecognised unit type" in new Fixture {
          val row = Row(rowKey = s"UNKNOWN~$UnitIdStr", fields = FamilyMap)

          UnitLinksNoPeriodRowMapper.fromRow(row) shouldBe None
        }
      }

      "when a child" - {
        "does not have an id" in new Fixture {
          val invalidUnitIdInChildField = FamilyMap.updated(ChildPrefix, CompaniesHouse)

          UnitLinksNoPeriodRowMapper.fromRow(Row(RowKey, fields = invalidUnitIdInChildField)) shouldBe None
        }

        "does not have a unit type" in new Fixture {
          val invalidUnitTypeInChildField = FamilyMap.updated(ChildPrefix + SampleCompaniesHouseChildId, "")

          UnitLinksNoPeriodRowMapper.fromRow(Row(RowKey, fields = invalidUnitTypeInChildField)) shouldBe None
        }

        "has an unrecognised unit type" in new Fixture {
          val invalidUnitTypeInChildField = FamilyMap.updated(ChildPrefix + SampleCompaniesHouseChildId, "UNKNOWN")

          UnitLinksNoPeriodRowMapper.fromRow(Row(RowKey, fields = invalidUnitTypeInChildField)) shouldBe None
        }
      }

      "when a parent" - {
        "does not have an id" in new Fixture {
          val invalidUnitIdInParentField = ChildrenMap + (ParentPrefix + Enterprise -> "")

          UnitLinksNoPeriodRowMapper.fromRow(Row(RowKey, fields = invalidUnitIdInParentField)) shouldBe None
        }

        "does not have a unit type" in new Fixture {
          val invalidUnitTypeInParentField = ChildrenMap + (ParentPrefix -> "111100003434")

          UnitLinksNoPeriodRowMapper.fromRow(Row(RowKey, fields = invalidUnitTypeInParentField)) shouldBe None
        }

        "has an unrecognised unit type" in new Fixture {
          val invalidUnitTypeInParentField = ChildrenMap + (ParentPrefix + "UNKNOWN" -> "111100003434")

          UnitLinksNoPeriodRowMapper.fromRow(Row(RowKey, fields = invalidUnitTypeInParentField)) shouldBe None
        }
      }
    }
  }
}
