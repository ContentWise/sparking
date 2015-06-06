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
object FlumeMultiPushBased {
  private val checkPointDirectory: String = "/tmp"

  def streaming(master:String, hosts: Array[String], ports: Array[Int]): Unit = {
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
    val streams = ( 0 to hosts.length - 1) map { p =>
      FlumeUtils.createStream(
        ssc,
        hosts(p),
        ports(p),
        StorageLevel.MEMORY_ONLY_SER_2)
    }

    val unifiedStream = ssc.union(streams)    // Merge the "per-partition" DStreams
    unifiedStream.repartition(1)              // Parallelism

    // Print out the count of events received from this server in each batch
    unifiedStream.count().map(cnt => "Received " + cnt + " flume events." ).print()

    ssc.start()
    ssc.awaitTermination()
  }

  def main(args:Array[String]): Unit = {
    Utils.fixLogging()
    if (args.length != 3) {
      System.err.println(
        "Usage: FlumeSinglePushBased <master> <hosts> <ports>")
      System.exit(1)
    }
    val hosts: Array[String] = args(1).split(",")
    val ports: Array[Int] = for ( port <- args(2).split(",") ) yield { port.toInt}
    FlumeMultiPushBased.streaming(args(0),hosts,ports)
  }
}
