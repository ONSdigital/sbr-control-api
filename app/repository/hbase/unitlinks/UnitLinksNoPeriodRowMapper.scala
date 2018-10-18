package repository.hbase.unitlinks

import com.typesafe.scalalogging.LazyLogging
import repository.RestRepository.{ ErrorMessage, Row, RowKey }
import repository.RowMapper
import repository.hbase.unitlinks.UnitLinksQualifier.{ ChildPrefix, ParentPrefix }
import uk.gov.ons.sbr.models.unitlinks.{ UnitId, UnitLinksNoPeriod, UnitType }
import utils.EitherSupport

object UnitLinksNoPeriodRowMapper extends RowMapper[UnitLinksNoPeriod] with LazyLogging {
  override def fromRow(row: Row): Option[UnitLinksNoPeriod] = {
    val (parentFields, childFields) = relationshipFieldsExcludingPrefixes(row)
    for {
      (unitId, unitType) <- UnitLinksRowKey.unapply(row.rowKey)
      parents <- validationErrorAsNone(row.rowKey, toRelatives(parentFields, validateParent))
      children <- validationErrorAsNone(row.rowKey, toRelatives(childFields, validateChild))
    } yield UnitLinksNoPeriod(unitId, unitType, toNonEmptyMap(parents), toNonEmptyMap(children))
  }

  /*
   * Partition fields into those defining parent links, and those defining child links.
   * Strip the parent / child prefix from column names.  This results in a pair of values that can be directly
   * converted to the relevant types (either a UnitId or a UnitType as appropriate).
   */
  private def relationshipFieldsExcludingPrefixes(row: Row): (Seq[(String, String)], Seq[(String, String)]) = {
    val (parentFields, childFields) = partitionRelationshipFields(row.rowKey, row.fields)
    val parentFieldsExcludingPrefix = mapFirst(parentFields.toSeq)(_.drop(ParentPrefix.length))
    val childFieldsExcludingPrefix = mapFirst(childFields.toSeq)(_.drop(ChildPrefix.length))
    (parentFieldsExcludingPrefix, childFieldsExcludingPrefix)
  }

  /*
   * Partition fields into those defining parent links, and those defining child links.
   * Any non-relationship fields (such as the edited flag) are dropped.
   */
  private def partitionRelationshipFields(rowKey: RowKey, fields: Map[String, String]): (Map[String, String], Map[String, String]) = {
    val (childFields, nonChildFields) = fields.partition { case (key, _) => key.startsWith(ChildPrefix) }
    val (parentFields, otherFields) = nonChildFields.partition { case (key, _) => key.startsWith(ParentPrefix) }
    if (otherFields.nonEmpty) {
      logger.warn(s"Dropping Unit Link database fields for rowKey [$rowKey] with qualifiers [${otherFields.keys}].")
    }
    (parentFields, childFields)
  }

  /*
   * Protect against case where the column name is just the child prefix with no trailing value; or in the case
   * of parents the cell is simply empty.
   * We do not enforce unitId formats here - just that there must be an id.
   */
  private def validateUnitId(str: String): Either[ErrorMessage, UnitId] =
    if (str.isEmpty) Left("UnitId cannot be empty") else Right(UnitId(str))

  private val validateUnitType: String => Either[String, UnitType] =
    UnitType.fromAcronym.andThen(Right.apply).
      applyOrElse(_: String, (str: String) => Left(s"Unrecognised UnitType [$str]"))

  /*
   * Applies a validation to each element of the pair, joining the validation results so that we only have
   * an output pair in the scenario where both transformations were valid.
   */
  private def validatePair[A, B, X, Y](fa: A => Either[ErrorMessage, X], fb: B => Either[ErrorMessage, Y])(pair: (A, B)): Either[ErrorMessage, (X, Y)] =
    pair match {
      case (a, b) => fa(a).right.flatMap { x =>
        fb(b).right.map { y =>
          x -> y
        }
      }
    }

  private val validateChild = validatePair(validateUnitId, validateUnitType) _
  private val validateParent = validatePair(validateUnitType, validateUnitId) _

  private def toRelatives[K, V](fields: Seq[(String, String)], f: ((String, String)) => Either[ErrorMessage, (K, V)]): Either[ErrorMessage, Seq[(K, V)]] =
    EitherSupport.sequence(fields.map(f))

  /*
   * Applies the supplied transformation function to the first element of each pair.
   */
  private def mapFirst[A, B, X](fields: Seq[(A, B)])(f: (A) => X): Seq[(X, B)] =
    fields.map { pair =>
      f(pair._1) -> pair._2
    }

  /*
   * Convert the validation result into an Option (so that it can be used by the for comprehension).
   * A validation error is translated to a None so that the for comprehension is aborted.
   */
  private def validationErrorAsNone[K, V](rowKey: RowKey, errorOrPairs: Either[ErrorMessage, Seq[(K, V)]]): Option[Seq[(K, V)]] = {
    errorOrPairs.left.foreach { errorMessage =>
      logger.error(s"Invalid unit links encountered for rowKey [$rowKey] - [$errorMessage]")
    }
    errorOrPairs.right.toOption
  }

  private def toNonEmptyMap[K, V](pairs: Seq[(K, V)]): Option[Map[K, V]] =
    if (pairs.isEmpty) None else Some(pairs.toMap)
}
