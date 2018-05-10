package repository.hbase.unitlinks

import scala.util.Try

import com.typesafe.scalalogging.LazyLogging

import uk.gov.ons.sbr.models.Period
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitLinks, UnitType }

import utils.TrySupport
import repository.RestRepository.Row
import repository.RowMapper
import repository.hbase.HBase.{ ChildOrParentPrefixLength, UnitChildPrefix, UnitParentPrefix }
import repository.hbase.unitlinks.UnitLinksRowKey.{ split, unitIdIndex, unitPeriodIndex, unitTypeIndex, unitLinksRowKeyLength }

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

      id = partitionedKey(unitIdIndex)
      unitTypeOpt = toUnitType(partitionedKey(unitTypeIndex))
      unitType <- unitTypeOpt
      periodOpt = toPeriod(partitionedKey(unitPeriodIndex))
      period <- periodOpt

      (partitionedParentMap, partitionedChildrenMap) = partitionMap(rows.fields)
      children = toFamilyMap(partitionedChildrenMap, UnitChildPrefix)(toChildField)
      parents = toFamilyMap(partitionedParentMap, UnitParentPrefix)(toParentField)

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

  private def toPeriod(periodStr: String): Option[Period] =
    toUnitLinksDataType(Period.parseString, periodStr)

  private def toUnitType(unitTypeAsStr: String): Option[UnitType] =
    toUnitLinksDataType(UnitType.fromString, unitTypeAsStr)

  private def toUnitLinksDataType[A](convertToDataType: String => Try[A], fieldAsStr: String): Option[A] =
    TrySupport.fold(convertToDataType(fieldAsStr))(failure =>
      failedUnitTypeRespAndLog(fieldAsStr)(failure), Some(_))

  private def toChildField(key: String, value: String, acc: Map[UnitId, UnitType]): Option[Map[UnitId, UnitType]] =
    toUnitType(value).map(unitType =>
      acc ++ Map(UnitId(key) -> unitType))

  private def toParentField(key: String, value: String, acc: Map[UnitType, UnitId]): Option[Map[UnitType, UnitId]] =
    toUnitType(key.drop(ChildOrParentPrefixLength)).map(unitType =>
      acc ++ Map(unitType -> UnitId(value)))

  private def toFamilyMap[A, B](variables: Map[String, String], prefixFilter: String)(
    validateAndReturnUnitType: (String, String, Map[A, B]) => Option[Map[A, B]]
  ): Option[Map[A, B]] =
    checkIfEmptyOptMap(variables.foldLeft[Option[Map[A, B]]](Some(Map.empty[A, B])) {
      case (acc, (k, v)) =>
        acc.flatMap { a =>
          if (k.startsWith(prefixFilter)) {
            validateAndReturnUnitType(v, k, a)
          } else Some(a)
        }
    })

  @deprecated("Migrated to toFamilyMap passing toChildField")
  private def toChildren(variables: Map[String, String]): Option[Map[UnitId, UnitType]] =
    checkIfEmptyOptMap(variables.foldLeft[Option[Map[UnitId, UnitType]]](Some(Map.empty[UnitId, UnitType])) {
      case (acc, (k, v)) =>
        acc.flatMap { a =>
          if (k.startsWith(UnitChildPrefix)) {
            toUnitType(v).map(unitType =>
              a ++ Map(UnitId(k) -> unitType))
          } else Some(a)
        }
    })

  @deprecated("Migrated to toFamilyMap passing toParentField")
  private def toParents(variables: Map[String, String]): Option[Map[UnitType, UnitId]] =
    checkIfEmptyOptMap(variables.foldLeft[Option[Map[UnitType, UnitId]]](Some(Map.empty[UnitType, UnitId])) {
      case (acc, (k, v)) =>
        acc.flatMap { a =>
          if (k.startsWith(UnitParentPrefix)) {
            toUnitType(k.drop(ChildOrParentPrefixLength)).map(unitType =>
              a ++ Map(unitType -> UnitId(v)))
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

  private def returnNoneWhenRowKeyIsNotOfLength(partitionedKey: Array[String]): Boolean =
    if (partitionedKey.length == unitLinksRowKeyLength) {
      logger.warn(s"Failure to produce UnitLinks, caused by rowKey [${partitionedKey.mkString}] invalid length " +
        s"[${partitionedKey.length}] when expected [$unitLinksRowKeyLength]")
      false
    } else true

}
