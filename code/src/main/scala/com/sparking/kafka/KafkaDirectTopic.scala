package com.sparking.kafka

import com.sparking.utils.Utils
import kafka.serializer.{StringDecoder, DefaultDecoder}
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/*
<b>Start Zookeeper & Broker</b>
  bin/zookeeper-server-start.sh config/zookeeper.properties
  bin/kafka-server-start.sh config/server.properties
<b>Start Topic</b>
  bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic <TOPIC_NAME>
<b>Start Simple Producer</b>
  bin/kafka-console-producer.sh --broker-list localhost:9092 --topic <TOPIC_NAME>
  every line is sent to topic
*/

/**
 * Created by darksch on 24/05/15.
 */
object KafkaDirectTopic {
  private var ssc: StreamingContext = _
  private val checkPointDirectory: String = "/tmp"
  private val consumerGroup: String = "KafkaSingleTopic_1"

  def streaming(master:String, topicNames:String, brokers:String) = {
    // Streaming batch interval 10 seconds
    val interval = Seconds(10)
    // Spark configuration
    val sparkConfiguration = {
      val configuration = new SparkConf()
        .setAppName("KafkaSingleTopic")
        .setMaster(master)
        .set("spark.streaming.unpersist", "true")
        .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      configuration
    }
    // Create Streaming Context and configurat checkpoint directory
    ssc = new StreamingContext(sparkConfiguration, interval)
    ssc.checkpoint(checkPointDirectory)
    // Create streams
    val kafkaStream = {
      val kafkaParameters = Map[String, String](
        "group.id" -> consumerGroup,
        "auto.offset.reset" -> "smallest",
        "zookeeper.connection.timeout.ms" -> "1000",
        "metadata.broker.list" -> brokers
      )
      val stream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
        ssc,                    // StreamingContext
        kafkaParameters,        // Kafka parameters
        topicNames.split(",").toSet    // Partitions for each topic
      ).map(_._2)
      stream
    }
    // Define the actual data flow of the streaming job
    kafkaStream.foreachRDD(rdd => {
      rdd.foreachPartition(partitionOfRecords => {
        partitionOfRecords.foreach ( x =>
          println(new String(x))
        )
      })
    })

    // Run the streaming job
    ssc.start()
    ssc.awaitTermination()
  }

  /**
   * 1. spark master
   * 2. topic names comma-separated
   * 3. zookeeper host:port
   */
  def main(args: Array[String]) {
    Utils.fixLogging()
    if ( args.length == 3 ) {
      KafkaDirectTopic.streaming(args(0),args(1),args(2))
    } else {
      println("KafkaSingleTopic <spark_master> <topic_names> <broker(s) host:port,host:port>")
    }
  }
}
