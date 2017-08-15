package utils

import java.util.Optional

import uk.gov.ons.sbr.data.domain.{ Enterprise, StatisticalUnit }

import scala.collection.JavaConversions._

/**
 * Created by haqa on 11/08/2017.
 */
object ScalaConversion {

  implicit class scalaOptionConversion(val o: Optional[Enterprise]) {
    def toOption: Option[Enterprise] =
      if (o.isPresent) Some(o.get) else None
  }

  implicit class scalaOptionListConversion(val l: Optional[java.util.List[StatisticalUnit]]) {
    def toOptionList: Option[List[StatisticalUnit]] =
      if (l.isPresent) Some(l.get.toList) else None
  }

}
