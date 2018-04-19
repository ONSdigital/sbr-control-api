package utils

import org.scalamock.scalatest.MockFactory
import org.scalatest.{ FreeSpec, Matchers }

import scala.util.{ Failure, Success }

class TrySupportSpec extends FreeSpec with Matchers with MockFactory {

  private trait Fixture {
    val onFailure = mockFunction[Throwable, String]
    val onSuccess = mockFunction[Int, String]
  }

  "TrySupport" - {
    "when folding a Try" - {
      "applies the onFailure function to a Failure" in new Fixture {
        val cause = new Exception()
        onFailure.expects(cause).returning("Goodbye World!")

        TrySupport.fold(Failure(cause))(onFailure, onSuccess) shouldBe "Goodbye World!"
      }

      "applies the onSuccess function to a Success" in new Fixture {
        onSuccess.expects(42).returning("Hello World!")

        TrySupport.fold(Success(42))(onFailure, onSuccess) shouldBe "Hello World!"
      }
    }
  }
}
