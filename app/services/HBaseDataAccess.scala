package services

/**
  * Created by coolit on 01/02/2018.
  */
class HBaseDataAccess extends DataAccess {
  def getUnitLinks(id: String): Int = ???

  def getUnitLinks(id: String, period: String): Int = ???

  def getEnterprise(id: String): Int = ???

  def getEnterprise(id: String, period: String): Int = ???

  def getStatUnitLink(id: String, category: String): Int = ???

  def getStatUnitLink(id: String, period: String, category: String): Int = ???
}
