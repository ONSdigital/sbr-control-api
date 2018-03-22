package unit

import resource.TestUtils
import uk.gov.ons.sbr.models._

/**
 * Created by coolit on 16/03/2018.
 */
class ParamsModelTestSpec extends TestUtils {

  private val validId = "12345678"
  private val invalidId = "1"
  private val validPeriod = "201802"
  private val validPeriodOpt = Some(validPeriod)
  private val invalidPeriod = "3000"

  "UnitLinksParams" should {
    "return Right(UnitLinkParams) if id param is valid" in {
      val unitLinks = UnitLinksParams(validId)
      UnitLinksParams.validate(validId) mustBe Right(unitLinks)
    }

    "return Left(InvalidId) if id param is invalid" in {
      UnitLinksParams.validate(invalidId) mustBe Left(InvalidId())
    }
  }

  "EnterpriseParams" should {
    "return Right(EnterpriseParams) if params are valid (no period)" in {
      val period = None
      val enterpriseParams = EnterpriseParams(validId, period)
      EnterpriseParams.validate(validId, period) mustBe Right(enterpriseParams)
    }

    "return Right(EnterpriseParams) if params are valid (with period)" in {
      val enterpriseParams = EnterpriseParams(validId, validPeriodOpt)
      EnterpriseParams.validate(validId, validPeriodOpt) mustBe Right(enterpriseParams)
    }

    "return Left(InvalidId) if id is invalid" in {
      EnterpriseParams.validate(invalidId, validPeriodOpt) mustBe Left(InvalidId())
    }

    "return Left(InvalidId) if period is invalid" in {
      val period = Some(invalidPeriod)
      EnterpriseParams.validate(validId, period) mustBe Left(InvalidPeriod())
    }
  }

  "StatUnitLinksParams" should {
    val validCategories: List[String] = List("ENT", "LEU", "VAT", "PAYE", "CH")

    validCategories.foreach(category => {
      s"return Right(StatUnitLinksParams) for valid params with category [$category]" in {
        val params = StatUnitLinksParams(validId, category, validPeriod)
        StatUnitLinksParams.validate(validId, category, validPeriod) mustBe Right(params)
      }
    })

    "return Left(InvalidId) if id is invalid" in {
      StatUnitLinksParams.validate(invalidId, validCategories.head, validPeriod) mustBe Left(InvalidId())
    }

    "return Left(InvalidPeriod) if period is invalid" in {
      StatUnitLinksParams.validate(validId, validCategories.head, invalidPeriod) mustBe Left(InvalidPeriod())
    }

    "return Left(InvalidCategory) if category is invalid" in {
      val invalidCategory = "enterprise"
      StatUnitLinksParams.validate(validId, invalidCategory, validPeriod) mustBe Left(InvalidCategory())
    }
  }
}
