package config

import play.api.Configuration
import com.typesafe.config.Config

/**
 * Properties
 * ----------------
 * Author: haqa
 * Date: 01 February 2018 - 12:19
 * Copyright (c) 2017  Office for National Statistics
 */
trait Properties {

  implicit val configuration: Configuration
  lazy private val config: Config = configuration.underlying

  lazy val dbConfig: Config = config.getConfig("db")

  lazy val requestTimeout: Int = config.getInt("play.ws.request.timeout")
  lazy val minKeyLength: Int = config.getInt("search.minKeyLength")

  // db
  lazy val defaultDBInit: String = dbConfig.getString("default.name")

}
