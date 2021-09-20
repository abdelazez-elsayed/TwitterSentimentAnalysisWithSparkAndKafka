package sentiment

import org.apache.spark.SparkConf
import org.apache.spark.ml.feature.Bucketizer
import org.apache.spark.sql.{SparkSession, functions}



object PredictionStream extends App with Logging {


    val topic: String = Settings.rawTopic
    val conf = new SparkConf
    if (!conf.contains("spark.master"))
      conf.setMaster("local[*]")
    println(s"Using Spark master '${conf.get("spark.master")}'")

    val spark = SparkSession
      .builder()
      .appName("TwitterSentimentAnalsis")
      .config(conf)
      .getOrCreate()

    // load the saved model from the distributed file system
    val model = org.apache.spark.ml.tuning.TrainValidationSplitModel.load(Settings.model_dir)

    //Start Spark Stream for reading from kafka topic
    val df1 = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", Settings.brokers)
      .option("subscribe", topic)
      .option("startingOffsets", "earliest")
      .option("failOnDataLoss", false)
      .option("maxOffsetsPerTrigger", 1000)
      .load()
    df1.printSchema()


    //Cast kafka value to string name it as tweet_text
    import spark.implicits._
    val df2 = df1.select($"value" cast "string" as "tweet_text")

    //make prediction
    val predictions = model.transform(df2)

    //Drop unwanted columns
    val df3 = predictions.drop("tweet_tokens_u","tweet_tokens","cv","features","rawPrediction")

    //Get first item in probability vector which is probability of bad sentiment and put in a column
    val firstItem = functions.udf((vec: org.apache.spark.ml.linalg.DenseVector) => vec(0))
    val df4= df3.withColumn("prob_not_good",firstItem(df3("probability"))).drop("probability")

    //For natural prediction we will take prediction in range [0.35,0.6] as natural (why this range ? No reason :D try another range)
    //We will use bucketizer to make three buckets for (positive , natural , negative)
    val bucketizer = new Bucketizer()
      .setInputCol("prob_not_good")
      .setOutputCol("value_double")
      .setSplits(Array(Double.NegativeInfinity,0.35
        ,0.6,Double.PositiveInfinity))
    //Drop unwanted columns now only remaining value (0, 1 or 2)
    val df5 = bucketizer.transform(df4).drop("tweet_text","prediction","prob_not_good")
    val df6 = df5.withColumn("value", $"value_double".cast("string")).drop("value_double")

    df6.printSchema()

    val writer = df6.writeStream
      .outputMode("update")
      .format("kafka")
      .option("kafka.bootstrap.servers", Settings.brokers)
      .option("topic",Settings.predictionTopic)
      .option("group.id", "testgroup")
      .option("checkpointLocation", "data_path/s")
      .start("data_path")
    writer.awaitTermination()






}
