package repository.hbase.unitlinks

import scala.util.Try

import com.typesafe.scalalogging.LazyLogging

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitLinks, UnitType }

import utils.TrySupport
import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.unitlinks.UnitLinksColumns.{ ChildOrParentPrefixLength, UnitChildPrefix, UnitParentPrefix }
import repository.hbase.unitlinks.UnitLinksRowKey._

object UnitLinksRowMapper extends RowMapper[UnitLinks] with LazyLogging {

  override def fromRow(rows: Row): Option[UnitLinks] =
    for {
      concatRowKey <- Option(rows.rowKey)
      partitionedKey = split(concatRowKey)

      /*
       * NOTE: GUARD - to not create UnitLinks in the event the rowKey is not of length 3
       * i.e. does not contain [UnitId] ~ [UnitType] ~ [Period] as String
       */
      if returnNoneWhenRowKeyIsNotOfLength(partitionedKey)

      id = partitionedKey(UnitIdIndex)
      unitTypeOpt = toUnitType(partitionedKey(UnitTypeIndex))
      unitType <- unitTypeOpt
      periodOpt = toPeriod(partitionedKey(UnitPeriodIndex))
      period <- periodOpt

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

  private def partitionMap(rawMap: Map[String, String]): (Map[String, String], Map[String, String]) =
    rawMap.partition {
      case (k, _) =>
        k.startsWith(UnitParentPrefix)
    }

  private def returnNoneIfAllNotPrefixedAsChild(rawMap: Map[String, String]): Boolean = !rawMap.map {
    case (key, _) =>
      if (key.startsWith(UnitChildPrefix)) true else {
        logger.warn(s"Bad field with key [$key] in partitioned (expected) children map [$rawMap]")
        false
      }
  }.toList.contains(false)

  private def toPeriod(periodStr: String): Option[Period] =
    toUnitLinksDataType(periodStr)(Period.parseString)

  private def toUnitType(unitTypeAsStr: String): Option[UnitType] =
    toUnitLinksDataType(unitTypeAsStr)(UnitType.fromString)

  private def toUnitLinksDataType[A](fieldAsStr: String)(convertToDataType: String => Try[A]): Option[A] =
    TrySupport.fold(convertToDataType(fieldAsStr))(failure =>
      failedUnitTypeRespAndLog(fieldAsStr)(failure), Some(_))

  private def toChildField(childMap: Map[String, String]): Option[Map[UnitId, UnitType]] =
    toFamilyMap(childMap, UnitChildPrefix) { (key: String, value: String, acc: Map[UnitId, UnitType]) =>
      toUnitType(value).map(unitType =>
        acc ++ Map(UnitId(key.drop(ChildOrParentPrefixLength)) -> unitType))
    }

  private def toParentField(parentMap: Map[String, String]): Option[Map[UnitType, UnitId]] =
    toFamilyMap(parentMap, UnitParentPrefix) { (key: String, value: String, acc: Map[UnitType, UnitId]) =>
      toUnitType(key.drop(ChildOrParentPrefixLength)).map(unitType =>
        acc ++ Map(unitType -> UnitId(value)))
    }

  private def toFamilyMap[A, B](variables: Map[String, String], prefixFilter: String)(
    validateAndReturnUnitType: (String, String, Map[A, B]) => Option[Map[A, B]]
  ): Option[Map[A, B]] =
    checkIfEmptyOptMap(variables.foldLeft[Option[Map[A, B]]](Some(Map.empty[A, B])) {
      case (acc, (key, value)) =>
        acc.flatMap { a =>
          if (key.startsWith(prefixFilter)) {
            validateAndReturnUnitType(key, value, a)
          } else Some(a)
        }
    })

  private def checkIfEmptyOptMap[A, B](optMapDefinedOrEmpty: Option[Map[A, B]]): Option[Map[A, B]] =
    optMapDefinedOrEmpty.flatMap { x =>
      if (x.nonEmpty) Some(x) else None
    }

  private def failedUnitTypeRespAndLog[B](invalidStr: String)(ex: Throwable): Option[B] = {
    logger.warn(s"Failed to create data type due to invalid field value [$invalidStr]. Failed with [$ex]")
    None
  }

  private def returnNoneWhenBothParentAndChildIsEmpty(
    children: Option[Map[UnitId, UnitType]],
    parents: Option[Map[UnitType, UnitId]]
  ): Boolean =
    if (children.isEmpty && parents.isEmpty) {
      logger.warn(s"Failure to produce UnitLinks, caused by children [$children] and parents [$parents] map being None")
      false
    } else true

  private def returnNoneWhenRowKeyIsNotOfLength(partitionedKey: List[String]): Boolean =
    if (partitionedKey.length != UnitLinksRowKeyLength) {
      logger.warn(s"Failure to produce UnitLinks, caused by rowKey [${partitionedKey.mkString}] invalid length " +
        s"[${partitionedKey.length}] when expected [$UnitLinksRowKeyLength]")
      false
    } else true

}
