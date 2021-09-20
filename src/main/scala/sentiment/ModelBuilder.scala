package sentiment

import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.SparkConf
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.{LogisticRegression, NaiveBayes}
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature.{Bucketizer, CountVectorizer, IDF, RegexTokenizer, StopWordsRemover}
import org.apache.spark.ml.tuning.{ParamGridBuilder, TrainValidationSplit}

object ModelBuilder {


  def main(args : Array[String]) {

    val inputfile = Settings.input_file
    val conf = new SparkConf
    if (!conf.contains("spark.master"))
      conf.setMaster("local[*]")
    println(s"Using Spark master '${conf.get("spark.master")}'")

    val spark = SparkSession
      .builder()
      .appName("TwitterSentimentAnalsis")
      .config(conf)
      .getOrCreate()

    val t1 = System.nanoTime
    try {
      //Read data
      val input = spark.read
        .option("header","false")
        .option("delimiter",",")
        .option("inferschema",value = true)
        .csv(inputfile)
        //Rename columns
        .toDF("label","tweet_id","tweet_datetime","query","user_name","tweet_text")
        //Drop unwated columns
        .drop("tweet_id")
        .drop("tweet_datetime")
        .drop("query")
        .drop("user_name")
      input.printSchema()
      input.show(5)

      //Split result to 2 buckets (Pos , natural)
      val bucketizer = new Bucketizer()
        .setInputCol("result")
        .setOutputCol("label")
        .setSplits(Array(Double.NegativeInfinity,4.0,
          Double.PositiveInfinity))


      //val df = bucketizer.transform(input)
      //getRandom(df,10).show(10)
      //Split input to training/test sets with %80/%20
      val seed = 1234L
      val Array(trainingData,testData) =input.randomSplit(Array(0.8,0.2),seed)

      //Convert text to tokens of words
      val tokenizer = new RegexTokenizer()
        .setInputCol("tweet_text")
        .setOutputCol("tweet_tokens_u")
        .setPattern("\\s+|[,.()\"]")

      //Remove stop words (I , a ,an the,..etc)
      val remover = new StopWordsRemover()
        .setStopWords(StopWordsRemover
          .loadDefaultStopWords("english"))
        .setInputCol("tweet_tokens_u")
        .setOutputCol("tweet_tokens")

      //Convert array of words tokens to to vector of words count
      //CountVectorizer perform TF
      val cv = new CountVectorizer()
        .setInputCol("tweet_tokens")
        .setOutputCol("cv")
        .setVocabSize(200000)
      //TF-IDF
      val idf = new IDF()
        .setInputCol("cv")
        .setOutputCol("features")

      val naiveBayes= new NaiveBayes()


     /*
      //Trying logistic regression
      val lr = new LogisticRegression()
        .setMaxIter(100)
        .setRegParam(0.02)
        .setElasticNetParam(0.3)

     val paramGrid = new ParamGridBuilder()
        .addGrid(lr.regParam,Array(0.01,0.02,0.1))
        .addGrid(lr.elasticNetParam,Array(0,0.3,0.5,0.8,1))
        .addGrid(lr.maxIter,Array(10,30,50,100))
        .build()
      //Making model pipline
      val steps =  Array( tokenizer, remover, cv, idf,lr)
      val pipeline = new Pipeline().setStages(steps)

      val crossValidator = new TrainValidationSplit()
        .setEstimator(pipeline)
        .setEvaluator(new BinaryClassificationEvaluator())
        .setEstimatorParamMaps(paramGrid)
        .setTrainRatio(0.8)
        .setParallelism(2)


      //Training the model

      //With crossValidator
      val model = crossValidator.fit(trainingData)
*/

      //Making model pipline
      val steps =  Array( tokenizer, remover, cv, idf,naiveBayes)
      val pipeline = new Pipeline().setStages(steps)
      //pipline model
       val model = pipeline.fit(trainingData)

      val t2 = System.nanoTime()
      println(s"building model took ${(t2-t1)*1E-9} seconds")

      var t3 = System.nanoTime()
      val predictions = model.transform(testData)
      var t4 = System.nanoTime()
      println(s"prediction took ${(t4-t3)*1E-9} seconds")
      val evaluator = new BinaryClassificationEvaluator()

      val accuracy = evaluator.evaluate(predictions)
      println(s"Test set accuracy = $accuracy")
      //Saving the model
      val model_save_dir = "model/naive_model/"
      model.write.overwrite().save(model_save_dir)
    } finally {
      spark.stop
    }
  }

}
