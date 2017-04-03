import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{IndexToString, StringIndexer, VectorIndexer}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.feature.PCA
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.ml.linalg.SparseVector

// Load the data stored in LIBSVM format as a DataFrame.
val tf = sc.textFile("/FileStore/tables/0g8s1g5r1490407313484/rst.data")
val temp=tf.map(l=>l.split(" ")).collect.map(l=>new LabeledPoint(l(0).toFloat,Vectors.dense(l(1).substring(2).toFloat,l(2).substring(2).toFloat,l(3).substring(2).toFloat,l(4).substring(2).toFloat,l(5).substring(2).toFloat)))

val dataRDD: RDD[LabeledPoint] = sc.parallelize(temp);

val pca = new PCA(4).fit(dataRDD.map(_.features))

val projected = dataRDD.map(p => p.copy(features = pca.transform(p.features)))

val data=projected.map(l=>(l.label.toFloat,new SparseVector(4,Array(0,1,2,3),Array(l.features(0).toDouble,l.features(1).toDouble,l.features(2).toDouble,l.features(3).toDouble)))).collect.toSeq.toDF("label","features")

// Index labels, adding metadata to the label column.
// Fit on whole dataset to include all labels in index.
val labelIndexer = new StringIndexer()
  .setInputCol("label")
  .setOutputCol("indexedLabel")
  .fit(data)
// Automatically identify categorical features, and index them.
// Set maxCategories so features with > 4 distinct values are treated as continuous.
val featureIndexer = new VectorIndexer()
  .setInputCol("features")
  .setOutputCol("indexedFeatures")
  .setMaxCategories(4)
  .fit(data)

 // Split the data into training and test sets (30% held out for testing).
val Array(trainingData, testData) = data.randomSplit(Array(0.8, 0.2))

// Train a RandomForest model.
val rf = new RandomForestClassifier()
  .setLabelCol("indexedLabel")
  .setFeaturesCol("indexedFeatures")
  .setNumTrees(10)

// Convert indexed labels back to original labels.
val labelConverter = new IndexToString()
  .setInputCol("prediction")
  .setOutputCol("predictedLabel")
  .setLabels(labelIndexer.labels)

// Chain indexers and forest in a Pipeline.
val pipeline = new Pipeline()
  .setStages(Array(labelIndexer, featureIndexer, rf, labelConverter))


// Train model. This also runs the indexers.
val model = pipeline.fit(trainingData)

// Make predictions.
val predictions = model.transform(testData)

// Select example rows to display.
predictions.select("predictedLabel", "label", "features").show(5)

// Select (prediction, true label) and compute test error.
val evaluator = new MulticlassClassificationEvaluator()
  .setLabelCol("indexedLabel")
  .setPredictionCol("prediction")
  .setMetricName("accuracy")
val accuracy = evaluator.evaluate(predictions)
println("Test Error = " + (1.0 - accuracy))

val rfModel = model.stages(2).asInstanceOf[RandomForestClassificationModel]
println("Learned classification forest model:\n" + rfModel.toDebugString)