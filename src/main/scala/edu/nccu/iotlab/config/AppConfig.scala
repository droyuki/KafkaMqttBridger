package edu.nccu.iotlab.config

import com.typesafe.config.{ConfigFactory, Config}

/**
  * Created by WeiChen on 2016/2/9.
  */
class AppConfig(config:Config) {
  config.checkValid(ConfigFactory.defaultReference(), "simple-lib")
  def this() {
    this(ConfigFactory.load())
  }
}
