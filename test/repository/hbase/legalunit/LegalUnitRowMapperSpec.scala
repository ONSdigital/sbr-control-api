package repository.hbase.legalunit

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{ FreeSpec, Matchers }
import repository.RestRepository.Row
import repository.hbase.legalunit.LegalUnitColumns._
import uk.gov.ons.sbr.models.Address
import uk.gov.ons.sbr.models.legalunit.{ Crn, LegalUnit, Ubrn, Uprn }

class LegalUnitRowMapperSpec extends FreeSpec with Matchers with LazyLogging {

  private trait Fixture {
    val UbrnValue = "1234567890123456"
    val NameValue = "Big Box Cereal Ltd"
    val TradingStyleValue = "BB Cereal"
    val Address1Value = "(Room 210)"
    val Address2Value = "Cherrys Road"
    val Address3Value = "Spotland"
    val Address4Value = "Maidenhead"
    val Address5Value = "Berkshire"
    val PostcodeValue = "SL6 1AZ"
    val Sic07Value = "10612"
    val PayeJobsValue = "34"
    val TurnoverValue = "90"
    val LegalStatusValue = "1"
    val TradingStatusValue = "A"
    val BirthDateValue = "01/01/2016"
    val DeathDateValue = "13/07/2018"
    val DeathCodeValue = "1"
    val CrnValue = "01245670"
    val UprnValue = "10023450178"

    val allVariables = Map(ubrn -> UbrnValue, name -> NameValue, tradingStyle -> TradingStyleValue, address1 -> Address1Value,
      address2 -> Address2Value, address3 -> Address3Value, address4 -> Address4Value, address5 -> Address5Value,
      postcode -> PostcodeValue, sic07 -> Sic07Value, payeJobs -> PayeJobsValue, turnover -> TurnoverValue,
      legalStatus -> LegalStatusValue, tradingStatus -> TradingStatusValue, birthDate -> BirthDateValue,
      deathDate -> DeathDateValue, deathCode -> DeathCodeValue, tradingStatus -> TradingStatusValue, crn -> CrnValue,
      uprn -> UprnValue)
    private val optionalColumns = Seq(tradingStyle, address2, address3, address4, address5, payeJobs, turnover,
      tradingStatus, deathDate, deathCode, crn, uprn)
    val mandatoryVariables = allVariables -- optionalColumns
    val UnusedRowKey = ""
  }

  "A LegalUnit row mapper" - {
    "can create a LegalUnit when all possible variables are defined" in new Fixture {
      LegalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables)) shouldBe Some(
        LegalUnit(ubrn = Ubrn(UbrnValue), name = NameValue, tradingStyle = Some(TradingStyleValue), sic07 = Sic07Value,
          payeJobs = Some(PayeJobsValue.toInt), turnover = Some(TurnoverValue.toInt), legalStatus = LegalStatusValue,
          tradingStatus = Some(TradingStatusValue), birthDate = BirthDateValue, deathDate = Some(DeathDateValue),
          deathCode = Some(DeathCodeValue), address = Address(line1 = Address1Value, line2 = Some(Address2Value),
            line3 = Some(Address3Value), line4 = Some(Address4Value), line5 = Some(Address5Value),
            postcode = PostcodeValue), crn = Some(Crn(CrnValue)), uprn = Some(Uprn(UprnValue)))
      )
    }

    "can create a LegalUnit when only the mandatory variables are defined" in new Fixture {
      LegalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = mandatoryVariables)) shouldBe Some(
        LegalUnit(ubrn = Ubrn(UbrnValue), name = NameValue, tradingStyle = None, sic07 = Sic07Value, payeJobs = None,
          turnover = None, legalStatus = LegalStatusValue, tradingStatus = None, birthDate = BirthDateValue,
          deathDate = None, deathCode = None, address = Address(line1 = Address1Value, line2 = None, line3 = None,
          line4 = None, line5 = None, postcode = PostcodeValue), crn = None, uprn = None)
      )
    }

    "cannot create a LegalUnit when" - {
      "a mandatory variable is missing" in new Fixture {
        val mandatoryColumns = mandatoryVariables.keys
        mandatoryColumns.foreach { column =>
          withClue(s"with missing column [$column]") {
            LegalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = mandatoryVariables - column)) shouldBe None
          }
        }
      }

      "the value of payeJobs is non-numeric" in new Fixture {
        LegalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(payeJobs, "invalid_int"))) shouldBe None
      }

      "the value of payeJobs is not an integral value" in new Fixture {
        LegalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(payeJobs, "3.14159"))) shouldBe None
      }

      "the value of turnover is non-numeric" in new Fixture {
        LegalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(turnover, "invalid_int"))) shouldBe None
      }

      "the value of turnover is not an integral value" in new Fixture {
        LegalUnitRowMapper.fromRow(Row(rowKey = UnusedRowKey, fields = allVariables.updated(turnover, "3.14159"))) shouldBe None
      }
    }
  }
}
