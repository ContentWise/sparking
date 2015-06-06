package com.sparking.flume

import com.sparking.utils.Utils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.flume.FlumeUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * Created by darksch on 24/05/15.
 */
object FlumeMultiPullBased {
  private val checkPointDirectory: String = "/tmp"

  def streaming(master: String, hosts: Array[String], ports: Array[Int]) = {
    val sparkConf = new SparkConf()
      .setAppName("FlumeSinglePullBased")
      .setMaster(master)
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    // Streaming micro batch interval
    val interval = Seconds(2)
    // Create StreamingContext
    val ssc: StreamingContext = new StreamingContext(sparkConf, interval)
    ssc.checkpoint(checkPointDirectory)
    // Create flume streams
    val flumeStreams = (0 to hosts.length - 1) map { p =>
      FlumeUtils.createPollingStream(ssc, hosts(p), ports(p))
    }
    // Merge streams
    val unifiedStream = ssc.union(flumeStreams)
    unifiedStream.repartition(1)              // Parallelism
    // Print out the count of events received from this server in each batch
    unifiedStream.count().map(cnt => "Received " + cnt + " flume events." ).print()

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
    val hosts: Array[String] = args(1).split(",")
    val ports: Array[Int] = for ( port <- args(2).split(",") ) yield { port.toInt}
    FlumeMultiPullBased.streaming(args(0),hosts,ports)
  }
}
