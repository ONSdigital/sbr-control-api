package utils

import play.api.libs.json.JsResult

object JsResultSupport {
  /*
   * Returns a JsSuccess containing the result of applying f to the parsed results when both results
   * are themselves a JsSuccess; otherwise returns JsError.
   */
  def map2[A, B, C](resultA: JsResult[A], resultB: JsResult[B])(f: (A, B) => C): JsResult[C] =
    resultA.flatMap { a =>
      resultB.map { b =>
        f(a, b)
      }
    }
}
