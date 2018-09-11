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

    val ParentsMapStr = Map(
      ParentPrefix + Enterprise -> SampleEnterpriseParentId
    )
    val ChildrenMapStr = Map(
      ChildPrefix + SampleCompaniesHouseChildId -> CompaniesHouse,
      ChildPrefix + SamplePayAsYouEarnChildId -> PayAsYouEarnTax
    )
    val FamilyMapStr = ParentsMapStr ++ ChildrenMapStr
  }

  "A UnitLinksNoPeriodRowMapper" - {
    "can create a UnitLinksNoPeriod from a row" - {
      "when all fields are available and valid" in new Fixture {
        val row = Row(RowKey, fields = FamilyMapStr)

        UnitLinksNoPeriodRowMapper.fromRow(row) shouldBe Some(SampleUnitLinksNoPeriodWithAllFields)
      }

      "when there are no children" in new Fixture {
        val row = Row(RowKey, fields = ParentsMapStr)

        UnitLinksNoPeriodRowMapper.fromRow(row) shouldBe Some(SampleUnitLinksNoPeriodWithAllFields.copy(children = None))
      }

      "when there are no parents" in new Fixture {
        val row = Row(RowKey, fields = ChildrenMapStr)

        UnitLinksNoPeriodRowMapper.fromRow(row) shouldBe Some(SampleUnitLinksNoPeriodWithAllFields.copy(parents = None))
      }
    }

    "cannot create a UnitLinksNoPeriod from a row" - {
      "when the row key is invalid" - {
        "because it contains too few elements" in new Fixture {
          val row = Row(rowKey = UnitIdStr, fields = FamilyMapStr)

          UnitLinksNoPeriodRowMapper.fromRow(row) shouldBe None
        }

        "because it contains too many elements" in new Fixture {
          val row = Row(rowKey = s"$RowKey~201801", fields = FamilyMapStr)

          UnitLinksNoPeriodRowMapper.fromRow(row) shouldBe None
        }

        "because it contains an unrecognised unit type" in new Fixture {
          val row = Row(rowKey = s"UNKNOWN~$UnitIdStr", fields = FamilyMapStr)

          UnitLinksNoPeriodRowMapper.fromRow(row) shouldBe None
        }
      }

      "when a child" - {
        "has an invalid unit type" in new Fixture {
          val invalidUnitTypeInChildField = FamilyMapStr.updated(ChildPrefix + SampleCompaniesHouseChildId, "UNKNOWN")

          UnitLinksNoPeriodRowMapper.fromRow(Row(RowKey, fields = invalidUnitTypeInChildField)) shouldBe None
        }
      }

      "when a parent" - {
        "has an invalid unit type" in new Fixture {
          val invalidUnitTypeInParentField = ChildrenMapStr + (ParentPrefix + "UNKNOWN" -> "111100003434")

          UnitLinksNoPeriodRowMapper.fromRow(Row(RowKey, fields = invalidUnitTypeInParentField)) shouldBe None
        }
      }

      "when the key prefix is neither that of a child or a parent" in new Fixture {
        val badPrefix = "x"
        val fieldKey = s"${badPrefix}_SomeNonPrefixValue"

        UnitLinksNoPeriodRowMapper.fromRow(Row(RowKey, fields = Map(fieldKey -> ""))) shouldBe None
      }

      "when there are neither parents or children" in new Fixture {
        UnitLinksNoPeriodRowMapper.fromRow(Row(RowKey, fields = Map.empty)) shouldBe None
      }
    }
  }
}
