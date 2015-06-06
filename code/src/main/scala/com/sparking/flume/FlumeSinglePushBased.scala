package com.sparking.flume

import com.sparking.utils.Utils
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.flume.FlumeUtils
import org.apache.spark.streaming.{StreamingContext, Seconds}

/**
 * Flume + Spark - Push-based Approach
 *
 *  After starting FlumeSinglePushBased you must start flume-ng agent with
 *  <b>sparkflume_push_single.conf</b> flume configuration, You can use the
 *  start.sh script from resources
 *  <i>sh start.sh spark sparkflume_push_single.conf DEBUG</i>
 *
 * Created by darksch on 24/05/15.
 */
object FlumeSinglePushBased {
  private val checkPointDirectory: String = "/tmp"

  def streaming(master:String, host: String, port: Int): Unit = {
    // Streaming micro batch interval
    val interval = Seconds(10)
    // Streaming context
    val sparkConf = new SparkConf()
      .setAppName("FlumeSinglePushBased")
      .setMaster(master)
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    // Create StreamingContext
    val ssc: StreamingContext = new StreamingContext(sparkConf, interval)
    ssc.checkpoint(checkPointDirectory)
    // Create a flume stream
    val stream = FlumeUtils.createStream(
      ssc,
      host,
      port,
      StorageLevel.MEMORY_ONLY_SER_2)

    // Print out the count of events received from this server in each batch
    stream.count().map(cnt => "Received " + cnt + " flume events." ).print()

    ssc.start()
    ssc.awaitTermination()
  }

  def main(args:Array[String]): Unit = {
    Utils.fixLogging()
    if (args.length != 3) {
      System.err.println(
        "Usage: FlumeSinglePushBased <master> <host> <port>")
      System.exit(1)
    }
    FlumeSinglePushBased.streaming(args(0),args(1),args(2).toInt)
  }
}
