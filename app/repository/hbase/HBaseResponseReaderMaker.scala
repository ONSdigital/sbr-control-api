package repository.hbase

import play.api.libs.json.Reads
import repository.RestRepository.Row

trait HBaseResponseReaderMaker {
  def forColumnGroup(columnGroup: String): Reads[Seq[Row]]
}

