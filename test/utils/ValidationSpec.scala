package utils

import support.TestUtils
import utils.Validation._

class ValidationSpec extends TestUtils {

  "validId" should {
    "return true for a valid id (integer)" in {
      val correctIdInt = "12345678"
      validId(correctIdInt) mustBe true
    }

    "return true for a valid id (chars)" in {
      val correctIdChars = "123456AB"
      validId(correctIdChars) mustBe true
    }

    "return false for an invalid id" in {
      val invalidId = "0"
      validId(invalidId) mustBe false
    }

    "return true for a valid id (lower end edge case)" in {
      val edgeCaseLowIdValid = "1234"
      validId(edgeCaseLowIdValid) mustBe true
    }

    "return false for an invalid id (lower end edge case)" in {
      val edgeCaseLowIdInvalid = "123"
      validId(edgeCaseLowIdInvalid) mustBe false
    }
  }

  "validPeriod" should {
    "return true for a valid period" in {
      val correctPeriod = "201802"
      validPeriod(correctPeriod) mustBe true
    }

    "return false for an invalid period (chars)" in {
      val invalidPeriodChars = "abcdef"
      validPeriod(invalidPeriodChars) mustBe false
    }

    "return false for an invalid period (no month)" in {
      val invalidPeriodYear = "3000"
      validPeriod(invalidPeriodYear) mustBe false
    }

    "return false for an invalid period (invalid month - too high)" in {
      val invalidPeriodTooHigh = "201813"
      validPeriod(invalidPeriodTooHigh) mustBe false
    }

    "return false for an invalid period (invalid month - too low)" in {
      val invalidPeriodTooLow = "201800"
      validPeriod(invalidPeriodTooLow) mustBe false
    }
  }

  "validCategory" should {
    val validCategories: List[String] = List("ENT", "LEU", "VAT", "PAYE", "CH", "LOU")

    validCategories.foreach(category => {
      s"return true for a valid category [$category]" in {
        validCategory(category) mustBe true
      }
    })

    "return false for an invalid category" in {
      val invalidCategory = "CRN"
      validCategory(invalidCategory) mustBe false
    }
  }
}
