# Twitter Stream Analysis using Spark ML and Kafka 
Most of this project is taken from this [repo](https://github.com/jpzk/twitterstream) with different implementaion for PredictionStream
##  Project Files
### ModelBuilder
Expressed in blog 
### IngestApp
Take from tweetSource and produce in kafka topic expressed in application.conf
### PredictionStream
Takes text from kafka topic produced from IngestApp and make a sentiment analysis using model built and saved in model directory then produce a prediction kafka topic
## sentiment_site
A NodeJS based server that reads from prediction topic and send it to web site 
## Twitter Hosebird Client: References

* https://dev.twitter.com/streaming/overview
* https://github.com/twitter/hbc

## Kafka Streams: References
 
### Official Documentation

* http://www.confluent.io/blog/introducing-kafka-streams-stream-processing-made-simple
* https://kafka.apache.org/documentation.html
* http://docs.confluent.io/3.0.0/streams/javadocs/index.html
* http://docs.confluent.io/3.0.0/streams/developer-guide.html#kafka-streams-dsl

### Other Code Examples

* https://github.com/bbejeck/kafka-streams
* https://github.com/confluentinc/examples/blob/kafka-0.10.0.0-cp-3.0.0/kafka-streams/src/main/scala/io/confluent/examples/streams/MapFunctionScalaExample.scala

### Articles

* http://codingjunkie.net/kafka-processor-part1/
* http://codingjunkie.net/kafka-streams-part2/
* http://codingjunkie.net/kafka-streams-machine-learning/
* https://dzone.com/articles/machine-learning-with-kafka-streams
