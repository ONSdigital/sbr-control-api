package utils

import org.scalamock.scalatest.MockFactory
import org.scalatest.{ FreeSpec, Matchers }
import play.api.libs.ws.WSResponse

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

class ResponseHandlerSpec extends FreeSpec with Matchers with MockFactory {

  private trait Fixture {
    val AwaitTime = 50.milliseconds
    type Result = String
    val DummyResult = "some-result"
    val DummyError = new Exception("some-exception")

    val response = stub[WSResponse]
    val successHandler = mockFunction[WSResponse, Result]

    // cannot mock PartialFunction directly - instead we delegate to a mock function
    // (see http://scalamock.org/user-guide/advanced_topics/ 'Partial Functions')
    val recoveryDelegate = mockFunction[Throwable, Result]
    val recoveryHandler = new PartialFunction[Throwable, Result] {
      override def isDefinedAt(x: Throwable): Boolean = true
      override def apply(cause: Throwable): Result = recoveryDelegate(cause)
    }

    val handler = ResponseHandler.make(successHandler)(recoveryHandler)
  }

  "make creates a response handler" - {
    "that invokes the supplied response handler function when the future succeeds with a value" in new Fixture {
      successHandler.expects(response).returning(DummyResult)

      Await.result(handler(Future.successful(response)), AwaitTime)
    }

    "that invokes the supplied recovery handler function when the future fails" in new Fixture {
      recoveryDelegate.expects(DummyError).returning(DummyResult)

      Await.result(handler(Future.failed(DummyError)), AwaitTime)
    }
  }
}
