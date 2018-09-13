package repository.hbase.unitlinks

import com.typesafe.scalalogging.LazyLogging
import repository.{ RestRepository, RowMapper }
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitLinksNoPeriod, UnitType }
import repository.hbase.unitlinks.UnitLinksQualifier.{ ChildPrefix, ParentPrefix }
import utils.TrySupport

import scala.util.Try

object UnitLinksNoPeriodRowMapper extends RowMapper[UnitLinksNoPeriod] with LazyLogging {
  override def fromRow(row: RestRepository.Row): Option[UnitLinksNoPeriod] = {
    val (partitionedParentMap, partitionedChildrenMap) = partitionMap(row.fields)
    for {
      (unitId, unitType) <- UnitLinksRowKey.unapply(row.rowKey)

      /*
       * NOTE: GUARD - to not create UnitLinks in the event the left partition is not
       * prefixed with child unit prefix
       */
      if returnNoneIfAllNotPrefixedAsChild(partitionedChildrenMap)
      children = toChildField(partitionedChildrenMap)
      if partitionedChildrenMap.isEmpty && children.isEmpty || partitionedChildrenMap.nonEmpty && children.isDefined
      parents = toParentField(partitionedParentMap)
      if partitionedParentMap.isEmpty && parents.isEmpty || partitionedParentMap.nonEmpty && parents.isDefined

      /*
       * NOTE: GUARD - to not create UnitLinks in the event children and parents is None.
       */
      if returnNoneWhenBothParentAndChildIsEmpty(children, parents)
    } yield UnitLinksNoPeriod(unitId, unitType, parents, children)
  }

  private def partitionMap(rawMap: Map[String, String]): (Map[String, String], Map[String, String]) =
    rawMap.partition { case (k, _) => k.startsWith(ParentPrefix) }

  private def returnNoneIfAllNotPrefixedAsChild(rawMap: Map[String, String]): Boolean =
    rawMap.forall { case (k, _) => k.startsWith(ChildPrefix) }

  private def toChildField(childMap: Map[String, String]): Option[Map[UnitId, UnitType]] =
    toFamilyMap(childMap, ChildPrefix) { (key: String, value: String) =>
      toUnitType(value).map(unitType =>
        UnitId(key.drop(ChildPrefix.length)) -> unitType)
    }

  private def toParentField(parentMap: Map[String, String]): Option[Map[UnitType, UnitId]] =
    toFamilyMap(parentMap, ParentPrefix) { (key: String, value: String) =>
      toUnitType(key.drop(ParentPrefix.length)).map(unitType =>
        unitType -> UnitId(value))
    }

  private def toFamilyMap[A, B](variables: Map[String, String], prefixFilter: String)(validateAndReturnUnitType: (String, String) => Option[(A, B)]): Option[Map[A, B]] =
    checkIfEmptyOptMap(variables.foldLeft[Option[Map[A, B]]](Some(Map.empty[A, B])) {
      case (acc, (key, value)) =>
        acc.flatMap { a =>
          if (key.startsWith(prefixFilter)) {
            validateAndReturnUnitType(key, value).map(a + _)
          } else Some(a)
        }
    })

  private def checkIfEmptyOptMap[A, B](optMapDefinedOrEmpty: Option[Map[A, B]]): Option[Map[A, B]] =
    optMapDefinedOrEmpty.filter(_.nonEmpty)

  private def toUnitType(unitTypeAsStr: String): Option[UnitType] =
    toUnitLinksDataType(unitTypeAsStr)(UnitType.fromString)

  private def toUnitLinksDataType[A](fieldAsStr: String)(convertToDataType: String => Try[A]): Option[A] =
    TrySupport.fold(convertToDataType(fieldAsStr))(failure =>
      failedUnitTypeRespAndLog(fieldAsStr)(failure), Some(_))

  private def failedUnitTypeRespAndLog[B](invalidStr: String)(ex: Throwable): Option[B] = {
    logger.warn(s"Failed to create data type due to invalid field value [$invalidStr]. Failed with [$ex]")
    None
  }

  private def returnNoneWhenBothParentAndChildIsEmpty(
    children: Option[Map[UnitId, UnitType]],
    parents: Option[Map[UnitType, UnitId]]
  ): Boolean = {
    val ifChildrenAndParentsIsEmpty = children.isEmpty && parents.isEmpty
    if (ifChildrenAndParentsIsEmpty) {
      logger.warn(s"Failure to produce UnitLinks, caused by children [$children] and parents [$parents] map being None")
    }
    !ifChildrenAndParentsIsEmpty
  }
}
