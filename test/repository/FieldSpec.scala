package repository

import org.scalamock.scalatest.MockFactory
import org.scalatest.{ FreeSpec, Matchers }
import org.slf4j.Logger
import repository.Field.{ Raw, Typed }

import scala.util.{ Failure, Success }

class FieldSpec extends FreeSpec with Matchers with MockFactory {

  private trait Fixture {
    val Employees = "employees"
    implicit val logger = mock[Logger]
  }

  private trait PresentFixture extends Fixture {
    val EmployeesValue = "42"
    val Variables = Map(Employees -> EmployeesValue)
  }

  private trait MissingFixture extends Fixture {
    val Variables = Map("jobs" -> "36")
  }

  private trait NonNumericFixture extends Fixture {
    val EmployeesValue = "non-numeric"
    val Variables = Map(Employees -> EmployeesValue)
  }

  "A raw field" - {
    "can be extracted from row variables by name" - {
      "when present" in new PresentFixture {
        Raw.named(Employees).apply(Variables) shouldBe Employees -> Some(EmployeesValue)
      }

      "when missing" in new MissingFixture {
        Raw.named(Employees).apply(Variables) shouldBe Employees -> None
      }
    }

    "when optional" - {
      "returns Some(string) when present" in new PresentFixture {
        Raw.optional(Employees -> Some(EmployeesValue)) shouldBe Some(EmployeesValue)
      }

      "returns None when missing" in new MissingFixture {
        Raw.optional(Employees -> None) shouldBe None
      }
    }

    "when mandatory" - {
      "returns Some(string) when present" in new PresentFixture {
        Raw.mandatory.apply(Employees -> Some(EmployeesValue)) shouldBe Some(EmployeesValue)
      }

      "logs when missing" in new MissingFixture {
        (logger.error(_: String)).expects(s"Mandatory field [$Employees] is missing.")

        Raw.mandatory.apply(Employees -> None) shouldBe None
      }
    }
  }

  "A typed field" - {
    "can be obtained by converting a raw field" - {
      "when present and the conversion is successful" in new PresentFixture {
        val successfulConversion = Success(123456L)

        Typed.tryConversion(_ => successfulConversion).apply(Employees -> Some(EmployeesValue)) shouldBe
          Employees -> Some(successfulConversion)
      }

      "when present and the conversion fails" in new PresentFixture {
        val failedConversion = Failure(new Exception("conversion failed"))

        Typed.tryConversion(_ => failedConversion).apply(Employees -> Some(EmployeesValue)) shouldBe
          Employees -> Some(failedConversion)
      }

      "when missing" in new Fixture {
        Typed.tryConversion(_ => Success(123456L)).apply(Employees -> None) shouldBe
          Employees -> None
      }
    }

    "when optional" - {
      "returns Success(Some(value)) when present and a successful conversion" in new Fixture {
        Typed.optional[Int].apply(Employees -> Some(Success(42))) shouldBe Success(Some(42))
      }

      "logs when present and a failed conversion" in new Fixture {
        val cause = new Exception("conversion failed")
        (logger.error(_: String, _: Throwable)).expects(s"Conversion attempt of field [$Employees] failed with cause [conversion failed].", cause)

        Typed.optional[Int].apply(Employees -> Some(Failure(cause))) shouldBe Failure(cause)
      }

      "returns Success(None) when missing" in new Fixture {
        Typed.optional[Int].apply(Employees -> None) shouldBe Success(None)
      }
    }

    "when mandatory" - {
      "returns Success(Some(value)) when present and a successful conversion" in new Fixture {
        Typed.mandatory[Int].apply(Employees -> Some(Success(42))) shouldBe Success(Some(42))
      }

      "logs when present and a failed conversion" in new Fixture {
        val cause = new Exception("conversion failed")
        (logger.error(_: String, _: Throwable)).expects(s"Conversion attempt of field [$Employees] failed with cause [conversion failed].", cause)

        Typed.mandatory[Int].apply(Employees -> Some(Failure(cause))) shouldBe Failure(cause)
      }

      "logs when missing" in new Fixture {
        (logger.error(_: String)).expects(s"Mandatory field [$Employees] is missing.")

        Typed.mandatory[Int].apply(Employees -> None) shouldBe a[Failure[_]]
      }
    }
  }

  "A conversion" - {
    "toInt" - {
      "succeeds when the field value represents a valid Int" in new PresentFixture {
        Field.Conversions.toInt(EmployeesValue) shouldBe Success(EmployeesValue.toInt)
      }

      "fails when the field value does not represent a valid Int" in new NonNumericFixture {
        Field.Conversions.toInt(EmployeesValue) shouldBe a[Failure[_]]
      }
    }
  }

  "A field that is" - {
    "an optionalStringNamed" - {
      "returns Some(string) when present" in new PresentFixture {
        Field.optionalStringNamed(Employees).apply(Variables) shouldBe Some(EmployeesValue)
      }

      "returns None when missing" in new MissingFixture {
        Field.optionalStringNamed(Employees).apply(Variables) shouldBe None
      }
    }

    "a mandatoryStringNamed" - {
      "returns Some(string when present)" in new PresentFixture {
        Field.mandatoryStringNamed(Employees).apply(Variables) shouldBe Some(EmployeesValue)
      }

      "logs when missing" in new MissingFixture {
        (logger.error(_: String)).expects(s"Mandatory field [$Employees] is missing.")

        Field.mandatoryStringNamed(Employees).apply(Variables) shouldBe None
      }
    }

    "an optionalIntNamed" - {
      "returns Success(Some(int)) when a value is present which represents a valid Int" in new PresentFixture {
        Field.optionalIntNamed(Employees).apply(Variables) shouldBe Success(Some(EmployeesValue.toInt))
      }

      "returns Success(None) when missing" in new MissingFixture {
        Field.optionalIntNamed(Employees).apply(Variables) shouldBe Success(None)
      }

      "logs when a value is present which does not represent a valid Int" in new NonNumericFixture {
        (logger.error(_: String, _: Throwable)).expects(where {
          (msg: String, _: Throwable) => msg.startsWith(s"Conversion attempt of field [$Employees] failed")
        })

        Field.optionalIntNamed(Employees).apply(Variables) shouldBe a[Failure[_]]
      }
    }

    "a mandatoryIntNamed" - {
      "returns Success(Some(int)) when a value is present which represents a valid Int" in new PresentFixture {
        Field.mandatoryIntNamed(Employees).apply(Variables) shouldBe Success(Some(EmployeesValue.toInt))
      }

      "logs when missing" in new MissingFixture {
        (logger.error(_: String)).expects(s"Mandatory field [$Employees] is missing.")

        Field.mandatoryIntNamed(Employees).apply(Variables) shouldBe a[Failure[_]]
      }

      "logs when a value is present which does not represent a valid Int" in new NonNumericFixture {
        (logger.error(_: String, _: Throwable)).expects(where {
          (msg: String, _: Throwable) => msg.startsWith(s"Conversion attempt of field [$Employees] failed")
        })

        Field.mandatoryIntNamed(Employees).apply(Variables) shouldBe a[Failure[_]]
      }
    }
  }
}
