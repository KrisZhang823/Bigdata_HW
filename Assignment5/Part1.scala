import org.apache.spark.graphx.GraphLoader
import org.apache.spark.graphx.{Graph, VertexRDD}
import org.apache.spark.graphx.util.GraphGenerators
import org.apache.spark._
import org.apache.spark.graphx._
// To make some of the examples work we will also need RDD
import org.apache.spark.rdd.RDD

// Load the edges as a graph
// val textFile = GraphLoader.edgeListFile(sc, "/FileStore/tables/y34ju3uc1491004380135/ca_HepTh-b2974.txt")
//
val textFile = sc.textFile("/FileStore/tables/hod97t611491074490759/ca_HepTh-b2974.txt").map(l=>l.split("\t"))

var vertices = textFile.map(l => (l(0).toLong,l(0))).distinct.collect

var edges = textFile.map(l => Edge(l(0).toLong,l(1).toLong,1)).collect

//Create rdd
val vRdd = sc.parallelize(vertices)
var eRdd = sc.parallelize(edges)

//Build graph
val graph = Graph(vRdd,eRdd)

// Define a reduce operation to compute the highest degree vertex
def max(a: (VertexId, Int), b: (VertexId, Int)): (VertexId, Int) = {
  if (a._2 > b._2) a else b
}

val maxInDegree: (VertexId, Int)  = graph.inDegrees.reduce(max)
println(maxInDegree)
val maxOutDegree: (VertexId, Int) = graph.outDegrees.reduce(max)
println(maxInDegree)


// Run PageRank
val ranks = graph.pageRank(0.0001).vertices
val topPages = ranks.takeOrdered(5)(Ordering[Double].reverse.on { x => x._2  })
topPages.foreach(println)

// Run Connected Components
val ccGraph = graph.connectedComponents() // No longer contains missing field
ccGraph.vertices.take(5).foreach(println)

// Find the triangle count for each vertex
val triCounts = graph.triangleCount().vertices
triCounts.takeOrdered(5)(Ordering[Long].reverse.on { x => x._2  }).foreach(println)




// Assign tweets to 1-second time windows.
dataset
  .where("created_at != null")
  .withColumn("text", clean($"text"))
  .withColumn("created_at", from_unixtime(unix_timestamp($"created_at", "EEE MMM dd HH:mm:ss ZZZZ yyyy")))
  .select(
    window('created_at, "1 second").getField("start") as 'start,
    'text)
  .as[Tweet]
  .flatMap { case tweet =>
    candidates.filter(tweet.text.toLowerCase.contains).map(candidate => candidate -> tweet.start)
  }
  .toDF("candidate", "start")
  .createOrReplaceTempView("tweets")






















