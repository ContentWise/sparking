package com.sparking.kafka

import com.sparking.utils.Utils
import kafka.serializer.DefaultDecoder
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/*
<b>Start Zookeeper & Broker</b>
  bin/zookeeper-server-start.sh config/zookeeper.properties
  bin/kafka-server-start.sh config/server.properties
<b>Start Topics</b>
  bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic <TOPIC_NAME_1>
  bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic <TOPIC_NAME_2>
<b>Start Simple Producer</b>
  bin/kafka-console-producer.sh --broker-list localhost:9092 --topic <TOPIC_NAME_1>
  bin/kafka-console-producer.sh --broker-list localhost:9092 --topic <TOPIC_NAME_2>
  every line is sent to topic
*/

/**
 * Created by darksch on 24/05/15.
 */
object KafkaMultiTopics {
  private var ssc: StreamingContext = _
  private val checkPointDirectory: String = "/tmp"
  private val consumerGroups: Array[String]= Array("KafkaSingleTopic_1","KafkaSingleTopic_2")

  def streaming(master:String, topicName:Array[String], zookeeper:String) = {
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
    val kafkaStreams = {
      val kafkaParameters = for (consumerGroup <- consumerGroups) yield {
        Map[String, String](
          "zookeeper.connect" -> zookeeper,
          "group.id" -> consumerGroup,
          "auto.offset.reset" -> "smallest",
          "zookeeper.connection.timeout.ms" -> "1000"
        )
      }
      val streams = (0 to kafkaParameters.length - 1) map { p =>
        KafkaUtils.createStream[Array[Byte], Array[Byte], DefaultDecoder, DefaultDecoder](
          ssc,                    // StreamingContext
          kafkaParameters(p),     // Kafka parameters
          Map(topicName(p) -> 1), // Partitions for each topic
          storageLevel = StorageLevel.MEMORY_ONLY_SER
        ).map(_._2)
      }

      val unifiedStream = ssc.union(streams)    // Merge the "per-partition" DStreams
      unifiedStream.repartition(1)              // Parallelism
    }

    // Define the actual data flow of the streaming job
    kafkaStreams.foreachRDD(rdd => {
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
   * 2. topics name comma separated
   * 3. zookeeper host:port
   */
  def main(args: Array[String]) {
    Utils.fixLogging()
    if ( args.length == 3 ) {
      KafkaMultiTopics.streaming(args(0),args(1).split(","),args(2))
    } else {
      println("KafkaSingleTopic <spark_master> <topic_name> <zookeeper host:port>")
    }
  }
}
