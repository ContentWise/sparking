package com.sparking.utils

import org.apache.log4j.{Level, Logger}

/**
 * Created by darksch on 24/05/15.
 */
object Utils {
  def fixLogging() = {
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)
  }
}
