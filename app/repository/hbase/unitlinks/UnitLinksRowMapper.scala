//package repository.hbase.unitlinks
//
//import uk.gov.ons.sbr.models.unitlinks.UnitLinks
//
//import repository.RestRepository.Row
//import repository.RowMapper
//import repository.hbase.HBase.RowKeyDelimiter
//
//object UnitLinksRowMapper extends RowMapper[UnitLinks] {
//
//  override def fromRow(variables: Row): Option[UnitLinks] = {
//
//    val rowKeyPartition = split(???)
//    if (rowKeyPartition.length == 3)
//      ???
//  }
//
//  private val split: Array[String] = (rowKey: String) =>
//    rowKey.split(RowKeyDelimiter)
//
//  private def toChildrenMap(variables: Row): Option[Map[String, String]] = {
//    variables.flatMap { case (x, y) => x }
//    variables.map(x => x)
//    variables.map { case (x, y) => x }
//    ???
//  }
//
//  private def toParentMap(variables: Row): Option[Map[String, String]] = ???
//
//}
