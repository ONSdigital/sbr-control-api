package uk.gov.ons.sbr.models

import scala.collection.JavaConversions._

import uk.gov.ons.sbr.data.domain.StatisticalUnit

/**
 * Created by haqa on 20/09/2017.
 */
object FamilyParser {

  def getChildrenMap(o: StatisticalUnit) = {
    o.getLinks.getChildren match {
      case x if !x.isEmpty =>
        Some(x.map { case (id, group) => id -> group.toString }.toMap)
      case _ => None
    }
  }

  def getParentMap(o: StatisticalUnit) = {
    o.getLinks.getParents match {
      case y if !y.isEmpty =>
        Some(y.map { case (group, id) => group.toString -> id }.toMap)
      case _ => None
    }
  }

}
