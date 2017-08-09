package utils

import java.time.{ DateTimeException, YearMonth }

/**
 * Created by haqa on 09/08/2017.
 */
sealed trait RequestEvaluation

case class IdRequest(id: String) extends RequestEvaluation
case class ReferencePeriod(id: String, period: YearMonth) extends RequestEvaluation
case class InvalidReferencePeriod(id: String, exception: DateTimeException) extends RequestEvaluation