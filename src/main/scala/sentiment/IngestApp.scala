package sentiment
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.{DefaultScalaModule, ScalaObjectMapper}

import sys.addShutdownHook
import org.apache.kafka.clients.producer.ProducerRecord


object IngestApp extends App with Logging {
  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  var closing = false

  // close gracefully
  addShutdownHook {
    closing = true
    producer.close
    source.hosebirdClient.stop
  }

  log.info(Settings.config.toString)

  val source = Settings.tweetSource
  val producer = Settings.kafkaProducer
  val topic = Settings.rawTopic
  val partition = Settings.partition

  while (!(source.hosebirdClient.isDone) & !(closing)) {
    source.take() match {
      case Some(json) =>
        send(json)
      case None =>
    }
  }

  def send(msg: String): Unit = {
    val ts = System.currentTimeMillis()
    //Parse Msg to json
    val parsedJson = mapper.readValue[Map[String, Object]](msg)
    //Extract only tweet_text to be streamed
    val text = parsedJson("text").toString
    val key = TweetKey(Settings.filterTerms)
    val keyPayload = Json.ByteArray.encode(key)
    val payload = text.map(_.toByte).toArray
    val record = new ProducerRecord[Array[Byte], Array[Byte]](topic, partition, ts, keyPayload, payload)
    log.info(s"Sending to Kafka ${record}")
    producer.send(record)
  }
}
