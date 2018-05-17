package repository.hbase.unitlinks

import org.scalamock.scalatest.MockFactory
import org.scalatest.{ FreeSpec, Matchers }

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.UnitType

import repository.RestRepository.Row
import repository.hbase.HBase.RowKeyDelimiter
import repository.hbase.unitlinks.UnitLinksProperties.{ UnitChildPrefix, UnitParentPrefix }
import repository.hbase.unitlinks.UnitLinksRowKey.split
import support.sample.SampleUnitLinks

class UnitLinksRowMapperSpec extends FreeSpec with Matchers with MockFactory {

  private trait Fixture extends SampleUnitLinks {
    val UnitIdStr = SampleUnitId.value
    val UnitTypeStr = UnitType.toAcronym(SampleUnitType)
    val PeriodStr = Period.asString(SamplePeriod)
    val RowKey = s"$UnitIdStr~$UnitTypeStr~$PeriodStr"

    val ParentsMapStr = Map(
      UnitParentPrefix + Enterprise -> SampleEnterpriseParentId
    )
    val ChildrenMapStr = Map(
      UnitChildPrefix + SampleCompaniesHouseChildId -> CompaniesHouse,
      UnitChildPrefix + SamplePayAsYouEarnChildId -> PayAsYourEarnTax
    )
    val FamilyMapStr = ParentsMapStr ++ ChildrenMapStr
  }

  "A Unit Links RowMapper" - {
    "can make a UnitLinks" - {
      "when all the fields are given" in new Fixture {
        UnitLinksRowMapper.fromRow(Row(rowKey = RowKey, fields = FamilyMapStr)) shouldBe Some(SampleUnitLinksWithAllFields)
      }
      "when only parents fields are found only" in new Fixture {
        UnitLinksRowMapper.fromRow(Row(rowKey = RowKey, fields = ParentsMapStr)) shouldBe Some(aUnitLinksSample(children = None))
      }
      "when only children fields are found only" in new Fixture {
        UnitLinksRowMapper.fromRow(Row(rowKey = RowKey, fields = ChildrenMapStr)) shouldBe Some(aUnitLinksSample(parents = None))
      }
    }

    "fails to construct a UnitLinks" - {
      "when the rowKey" - {
        "is not of length 3" in new Fixture {
          val shortRowKey = split(RowKey).drop(1).mkString(RowKeyDelimiter)
          UnitLinksRowMapper.fromRow(Row(rowKey = shortRowKey, fields = FamilyMapStr)) shouldBe None
        }
        "contains an invalid unitType" in new Fixture {
          val rowKeyWithBadUnitType = split(RowKey).updated(1, "UNKNOWN").mkString(RowKeyDelimiter)
          UnitLinksRowMapper.fromRow(Row(rowKey = rowKeyWithBadUnitType, fields = FamilyMapStr)) shouldBe None
        }
        "contains an invalid period" in new Fixture {
          val rowKeyWithInvalidPeriod = split(RowKey).updated(2, "201800").mkString(RowKeyDelimiter)
          UnitLinksRowMapper.fromRow(Row(rowKey = rowKeyWithInvalidPeriod, fields = FamilyMapStr)) shouldBe None
        }
      }

      "when the UnitLinks field index" - {
        "have a child field with an invalid unit type" in new Fixture {
          val invalidUnitTypeInChildField = FamilyMapStr.updated(UnitChildPrefix + SampleCompaniesHouseChildId, "UNKNOWN")
          UnitLinksRowMapper.fromRow(Row(rowKey = RowKey, fields = invalidUnitTypeInChildField)) shouldBe None
        }
        "have a parent field with an invalid unit type" in new Fixture {
          val invalidUnitTypeInParentField = ChildrenMapStr + (UnitParentPrefix + "UNKNOWN" -> "111100003434")
          UnitLinksRowMapper.fromRow(Row(rowKey = RowKey, fields = invalidUnitTypeInParentField)) shouldBe None
        }
      }

      "when the field map key index" - {
        "include neither a record of child or parent prefix" in new Fixture {
          UnitLinksRowMapper.fromRow(Row(rowKey = RowKey, fields = Map(s"s_$SampleCompaniesHouseChildId" -> ""))) shouldBe None
        }
      }
    }
  }

}
