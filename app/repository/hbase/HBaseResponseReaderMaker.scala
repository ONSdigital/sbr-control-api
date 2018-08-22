package repository.hbase

import play.api.libs.json.Reads
import repository.RestRepository.Row

trait HBaseResponseReaderMaker {
  def forColumnFamily(columnFamily: String): Reads[Seq[Row]]
}

