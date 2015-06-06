# Spark Streaming

Spark Streaming is an extension of the core Spark API that enables scalable, high-throughput, fault-tolerant stream processing of live data streams. Data can be ingested from many sources like Kafka, Flume, ZeroMQ, Kinesis or TCP sockets can be processed using complex algorithms expressed with high-level functions like map, reduce, join and window.

Spark Streaming receives live input data streams and divides the data into micro batches, which are then processed by the Spark engine to generate the final stream of results in batches.

Spark Streaming is available through Maven Central.
### Maven
```
<dependency>
    <groupId>org.apache.spark</groupId>
    <artifactId>spark-streaming_2.10</artifactId>
    <version>1.3.1</version>
</dependency>
```
### SBT
`libraryDependencies += "org.apache.spark" % "spark-streaming_2.10" % "1.3.0"`

For ingesting data from sources like Kafka or Flume, that are not part of the Spark Streaming core API, you need to add the corresponding artifact `spark-streaming-xyz_2.10` that includes all required classes to integrate Spark Streaming with the selected source.

## Kafka
#### SBT
`libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka" % "1.3.0"`

## Flume
#### SBT
`libraryDependencies += "org.apache.spark" %% "spark-streaming-flume" % "1.3.0"`

## Kafka Integration
There are two approaches to integrate Kafka with Spark Streaming
 1. the old approach using Receivers and Kafka’s high-level API
 2. a new experimental approach (introduced in Spark 1.3) without using Receivers. 
They have different programming models, performance characteristics, and semantics guarantees. 

### Receiver-based approach
This approach uses a Receiver to receive the data, which is implemented using the Kafka high-level consumer API. The data received from Kafka through a Receiver is stored in Spark executors, and then data is processed.

Under default configuration, this approach can lose data under failures, to ensure zero-data loss, you have to additionally enable Write Ahead Logs in Spark Streaming, the WAL synchronously saves all the received Kafka data into write ahead logs on a distributed file system so all data can be recovered on failure.

Examples for this approach are:
 * KafkaSingleTopic
 * KafkaMultiTopics

These are the steps required to setup a kafka environment to test examples:
* **Start zookeeper & Kafka Broker**
  * `bin/zookeeper-server-start.sh config/zookeeper.properties`
  * `bin/kafka-server-start.sh config/server.properties`
* **Create topic(s)**: 
  * `bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic <TOPIC_NAME>` (for multi topics example create two topic with different name)
* ***Start producer**
  *  you can use any producer, one of the simplest and that comes with kafka installation is the console producer that can be useful for testing purpose `bin/kafka-console-producer.sh --broker-list localhost:9092 --topic <TOPIC_NAME>`. Once started it will send any line you entered in the console.

Kafka examples require the following input parameters:
 * **KafkaSingleTopic**:
  * _<spark_master>_: local[*], local[3] or spark://host:port 
  * _<topic_name>_: name of kafka topic
  * _<zookeeper host:port>_: host:port of zookeeper server (e.g. localhost:2181)
 * **KafkaMultiTopics**
  * _<spark_master>_: local[*], local[3] or spark://host:port 
  * _<topic_names>_: a comma separated string of topic names
  * _<zookeeper host:port>_: host:port of zookeeper server (e.g. localhost:2181)

**Level of Parallelism in Data Receiving**

Receiving data over the network (like Kafka, Flume, socket, etc.) requires the data to deserialized and stored in Spark. 

If the data receiving becomes a bottleneck in the system, then consider parallelizing the data receiving. **Note that each input DStream creates a single receiver (running on a worker machine) that receives a single stream of data**.

Receiving multiple data streams can therefore be achieved by creating multiple input DStreams and configuring them to receive different partitions of the data stream from the source(s). An example is Kafka input DStream receiving two topics of data, it can be split into two Kafka input streams, each receiving only one topic. This would run **two receivers on two workers**, thus allowing data to be received in parallel, and increasing overall throughput. These multiple DStream can be unioned together to create a single DStream (see Kafka multiple topic example)

### Direct approach
`Work in progress...`

## Flume Integration
There are two approaches to integrate flume with Spark Streaming
 1. Push-based approach
 2. Pull-based approach

### Push-based approach
In this approach, Spark Streaming **sets up a receiver** that acts as an **Avro agent** for Flume. You need:
 1. a Spark worker to run on a specific machine (used in Flume configuration)
 2. create an Avro sink in your Flume configuration to push data to a port on that machine 
     ```
     agent.sinks = avroSink
     agent.sinks.avroSink.type = avro
     agent.sinks.avroSink.channel = memoryChannel
     agent.sinks.avroSink.hostname = localhost
     agent.sinks.avroSink.port = 33333
    ```

### Pull-based approach
Instead of Flume pushing data directly to Spark Streaming, this approach runs a custom Flume sink allowing:
 * Flume to **push data into the sink**, and data stays buffered
 * Spark Streaming uses a reliable Flume receiver and transaction **to pull data from the sink**. This solution **guarantees** that a transaction succeeds only after data is recevide and replicated by Spark Streaming
Therefore this solution guarantees **stronger reliability and fault-tolerance** and should be preferred when these requirements are mandatory, the difference with respect to the push-based approach is that you are required to configure Flume to run a custom sink.

To setup this configuration you need to:
 * select a machine that will run the custom sink in a Flume agent, this is where the Flume pipeline is configured to send data.
 * the Spark Streaming - Flume integration jar contains the custom sink implementation and it must be used to configure a Flume sink like
  ```
  agent.sinks = spark
  agent.sinks.spark.type = org.apache.spark.streaming.flume.sink.SparkSink
  agent.sinks.spark.hostname = localhost
  agent.sinks.spark.port = 33333
  agent.sinks.spark.channel = memoryChannel
  ```

#### Examples
Examples for these approaches are:
 * FlumeMultiPullBased
 * FlumeSinglePullBased
 * FlumeMultiPushBased
 * FlumeSinglePushBased

examples of flume configurations are provided in resources folder, you can also find a start.sh script that can be used to start Flume agent. 

##### Push-based
To execute push-based examples you need to:
 1. start Spark Streaming example. It creates a sink to which flume will connect to
 2. start Flume pipeline, the provided configurations use a Flume source that monitors a file for new input lines

##### Pull-based
To execute pull-based examples you need to:
 1. start Flume agent, it creates the pipeline with the configured custom sink
 2. start Spark Streaming example. It connects to the custom sink to retrieve data


