package sentiment


import com.typesafe.config.ConfigFactory
import java.io.File
import java.util.{Properties, UUID}

import com.twitter.hbc.httpclient.auth.OAuth1
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.KStreamBuilder
object Settings {
  val config = ConfigFactory.parseFile(new File("application.conf"))
  val kConfig = config.getConfig("kafka")
  val tConfig = config.getConfig("twitter")
  val mConfig = config.getConfig("model")
  def filterTerms = {
    val terms = tConfig.getStringList("terms")
    Range(0, terms.size()).map { i => terms.get(i) }
  }

  def zookeepers = kConfig.getString("zookeepers")

  def brokers = kConfig.getString("brokers")

  def rawTopic = kConfig.getString("raw_topic")
  def predictionTopic = kConfig.getString("prediciton_topic")

  def partition = kConfig.getInt("partition")

  def stateDir = kConfig.getString("state_dir")
  def model_dir = mConfig.getString("model_dir")
  def input_file = mConfig.getString("input_file")
  def kafkaProducer = {
    val props = new Properties()
    val serde = "org.apache.kafka.common.serialization.ByteArraySerializer"
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ProducerConfig.ACKS_CONFIG, "all")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, serde)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serde)
    new KafkaProducer[Array[Byte], Array[Byte]](props)
  }



  def tweetSource() = {
    val oAuth1 = new OAuth1(
      tConfig.getString("consumer_key"),
      tConfig.getString("consumer_secret"),
      tConfig.getString("token"),
      tConfig.getString("token_secret"))

    new TweetsSource(oAuth1, filterTerms)
  }
}