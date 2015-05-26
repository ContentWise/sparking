name := "SparkStreaming"

version := "0.1"

scalaVersion := "2.10.4"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.3.0"

libraryDependencies +=  "org.scalatest" % "scalatest_2.10.0-M4" % "1.9-2.10.0-M4-B1"

libraryDependencies +=  "junit" % "junit" % "4.8.1" % "test"

//libraryDependencies += "net.sourceforge.jregex" % "jregex" % "1.2_01"

//libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.3.0"

//libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.3.0"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "1.3.0"

//libraryDependencies += "org.apache.spark" %% "spark-streaming-twitter" % "1.3.0"

libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka" % "1.3.0"

libraryDependencies += "org.apache.spark" %% "spark-streaming-flume" % "1.3.0"

//libraryDependencies += "com.google.code.gson" % "gson" % "2.3"

//libraryDependencies += "org.twitter4j" % "twitter4j-core" % "3.0.3"

//libraryDependencies += "commons-cli" % "commons-cli" % "1.2"

//libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1"

//libraryDependencies += "org.apache.ctakes" % "ctakes-core" % "3.2.1"

//libraryDependencies += "org.apache.ctakes" % "ctakes-core-res" % "3.2.1"

//libraryDependencies += "org.apache.ctakes" % "ctakes-constituency-parser" % "3.2.1"

//libraryDependencies += "org.apache.ctakes" % "ctakes-clinical-pipeline" % "3.2.1"

//libraryDependencies += "org.apache.ctakes" % "ctakes-dictionary-lookup-fast" % "3.2.1"

//libraryDependencies += "org.apache.ctakes" % "ctakes-drug-ner" % "3.2.1"

//libraryDependencies += "org.apache.ctakes" % "ctakes-assertion" % "3.2.1"

//libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1"

javaOptions += "-Xmx2G"

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"

resolvers += "opennlp sourceforge repo" at "http://opennlp.sourceforge.net/maven2"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/releases/"

