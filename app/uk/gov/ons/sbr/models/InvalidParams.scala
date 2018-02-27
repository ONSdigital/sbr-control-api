package uk.gov.ons.sbr.models

/**
 * Created by coolit on 19/02/2018.
 *
 * Note: Since we hard code the msg string these case classes could be case objects, however in future
 * parameters such as the correct reference period format could be passed in to make errors more clear.
 *
 */
sealed trait InvalidParams {
  val msg: String
}
case class InvalidId(msg: String = "controller.invalid.id") extends InvalidParams
case class InvalidPeriod(msg: String = "controller.invalid.period") extends InvalidParams
case class InvalidCategory(msg: String = "controller.invalid.category") extends InvalidParams
