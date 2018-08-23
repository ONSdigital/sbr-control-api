package utils

import play.api.libs.json.JsResult

object JsResultSupport {
  def map2[A, B, C](resultA: JsResult[A], resultB: JsResult[B])(f: (A, B) => C): JsResult[C] =
    resultA.flatMap { a =>
      resultB.map { b =>
        f(a, b)
      }
    }
}
