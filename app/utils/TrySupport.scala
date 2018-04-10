package utils

import scala.util.{ Failure, Success, Try }

/*
 * Remove this and use the standard library functions once on Scala 2.12
 */
object TrySupport {
  def fold[A, B](aTry: Try[A])(onFailure: Throwable => B, onSuccess: A => B): B =
    aTry match {
      case Success(a) => onSuccess(a)
      case Failure(t) => onFailure(t)
    }
}
