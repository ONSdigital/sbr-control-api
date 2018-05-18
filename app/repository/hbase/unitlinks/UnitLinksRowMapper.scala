package repository.hbase.unitlinks

import scala.util.Try

import com.typesafe.scalalogging.LazyLogging

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitLinks, UnitType }

import utils.TrySupport
import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.unitlinks.UnitLinksProperties.{ UnitChildPrefix, UnitParentPrefix }
import repository.hbase.unitlinks.UnitLinksRowKey._

object UnitLinksRowMapper extends RowMapper[UnitLinks] with LazyLogging {

  override def fromRow(rows: Row): Option[UnitLinks] =
    for {
      partitionedKey <- splitRowKey(rows.rowKey)(logger)
      id = partitionedKey(unitIdIndex)
      unitTypeOpt = toUnitType(partitionedKey(unitTypeIndex))
      unitType <- unitTypeOpt
      periodOpt = toPeriod(partitionedKey(unitPeriodIndex))
      period <- periodOpt
      t = test(partitionedKey.mkString)

      (partitionedParentMap, partitionedChildrenMap) = partitionMap(rows.fields)

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

    } yield UnitLinks(UnitId(id), period, parents, children, unitType)

  private def test(t: String) = print(t + " ===== "); false
  private def partitionMap(rawMap: Map[String, String]): (Map[String, String], Map[String, String]) =
    rawMap.partition { case (k, _) => k.startsWith(UnitParentPrefix) }

  private def returnNoneIfAllNotPrefixedAsChild(rawMap: Map[String, String]): Boolean =
    rawMap.forall { case (k, _) => k.startsWith(UnitChildPrefix) }

  private def toPeriod(periodStr: String): Option[Period] =
    toUnitLinksDataType(periodStr)(Period.parseString)

  private def toUnitType(unitTypeAsStr: String): Option[UnitType] =
    toUnitLinksDataType(unitTypeAsStr)(UnitType.fromString)

  private def toUnitLinksDataType[A](fieldAsStr: String)(convertToDataType: String => Try[A]): Option[A] =
    TrySupport.fold(convertToDataType(fieldAsStr))(failure =>
      failedUnitTypeRespAndLog(fieldAsStr)(failure), Some(_))

  private def toChildField(childMap: Map[String, String]): Option[Map[UnitId, UnitType]] =
    toFamilyMap(childMap, UnitChildPrefix) { (key: String, value: String) =>
      toUnitType(value).map(unitType =>
        UnitId(key.drop(UnitChildPrefix.length)) -> unitType)
    }

  private def toParentField(parentMap: Map[String, String]): Option[Map[UnitType, UnitId]] =
    toFamilyMap(parentMap, UnitParentPrefix) { (key: String, value: String) =>
      toUnitType(key.drop(UnitParentPrefix.length)).map(unitType =>
        unitType -> UnitId(value))
    }

  private def toFamilyMap[A, B](variables: Map[String, String], prefixFilter: String)(
    validateAndReturnUnitType: (String, String) => Option[(A, B)]
  ): Option[Map[A, B]] =
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

  private def failedUnitTypeRespAndLog[B](invalidStr: String)(ex: Throwable): Option[B] = {
    logger.warn(s"Failed to create data type due to invalid field value [$invalidStr]. Failed with [$ex]")
    None
  }

  private def returnNoneWhenBothParentAndChildIsEmpty(children: Option[Map[UnitId, UnitType]],
    parents: Option[Map[UnitType, UnitId]]): Boolean = {
    val ifChildrenAndParentsIsEmpty = children.isEmpty && parents.isEmpty
    if (ifChildrenAndParentsIsEmpty) {
      logger.warn(s"Failure to produce UnitLinks, caused by children [$children] and parents [$parents] map being None")
    }
    !ifChildrenAndParentsIsEmpty
  }
}
