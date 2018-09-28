package repository

import repository.RestRepository.{ ErrorMessage, Field, Row, RowKey }
import repository.hbase.Column

import scala.concurrent.Future

sealed trait OptimisticEditResult
sealed trait CreateOrReplaceResult

object EditApplied extends OptimisticEditResult with CreateOrReplaceResult
object EditConflicted extends OptimisticEditResult
object EditTargetNotFound extends OptimisticEditResult
object EditFailed extends OptimisticEditResult with CreateOrReplaceResult

trait RestRepository {
  def findRow(table: String, rowKey: RowKey, columnFamily: String): Future[Either[ErrorMessage, Option[Row]]]
  def findRows(table: String, query: String, columnFamily: String): Future[Either[ErrorMessage, Seq[Row]]]

  def createOrReplace(table: String, rowKey: RowKey, field: Field, otherFields: Field*): Future[CreateOrReplaceResult]
  def updateField(table: String, rowKey: RowKey, checkField: Field, updateField: Field, otherUpdateFields: Field*): Future[OptimisticEditResult]
  def deleteField(table: String, rowKey: RowKey, checkField: Field, columnName: Column): Future[OptimisticEditResult]
}

object RestRepository {
  type ErrorMessage = String
  type RowKey = String
  type Field = (Column, String)

  case class Row(rowKey: RowKey, fields: Map[String, String])
}