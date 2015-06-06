package com.sparking.flume

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.flume.FlumeUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import com.sparking.utils.Utils

/**
 * Created by darksch on 24/05/15.
 */
object FlumeSinglePullBased {
  private val checkPointDirectory: String = "/tmp"

  def streaming(master: String, host: String, port: Int) = {
    val sparkConf = new SparkConf()
      .setAppName("FlumeSinglePullBased")
      .setMaster(master)
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    // Streaming micro batch interval
    val interval = Seconds(10)
    // Create StreamingContext
    val ssc: StreamingContext = new StreamingContext(sparkConf, interval)
    ssc.checkpoint(checkPointDirectory)

    val flumeStream = FlumeUtils.createPollingStream(ssc, host, port)
    // Print out the count of events received from this server in each batch
    flumeStream.count().map(cnt => "Received " + cnt + " flume events." ).print()

    // Run the streaming job
    ssc.start()
    ssc.awaitTermination()
  }

  def main(args: Array[String]) {
    Utils.fixLogging()
    if ( args.length != 3 ) {
      System.err.println(
        "Usage: FlumeSinglePullBased <master> <host> <port>")
      System.exit(1)
    }
    FlumeSinglePullBased.streaming(args(0),args(1),args(2).toInt)
  }
}
