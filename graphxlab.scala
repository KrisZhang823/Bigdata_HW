import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

println(spark.version)
//
case class Person(name:String,age:Int)

val defaultPerson = Person("NA",0)

val vertexList = List(
  (1L, Person("Alice", 18)),
  (2L, Person("Bernie", 17)),
  (3L, Person("Cruz", 15)),
  (4L, Person("Donald", 12)),
  (5L, Person("Ed", 15)),
  (6L, Person("Fran", 10)),
  (7L, Person("Genghis",854))
)

val edgeList = List(
  Edge(1L, 2L, 5),
  Edge(1L, 3L, 1),
  Edge(3L, 2L, 5),
  Edge(2L, 4L, 12),
  Edge(4L, 5L, 4),
  Edge(5L, 6L, 2),
  Edge(6L, 7L, 2),
  Edge(7L, 4L, 5),
  Edge(6L, 4L, 4)
)

val vertexRDD = sc.parallelize(vertexList)
val edgeRDD = sc.parallelize(edgeList)
val graph = Graph(vertexRDD, edgeRDD,defaultPerson)

println("Edges = " + graph.edges.count())
println("Vertices = " + graph.vertices.count())
val vertices = graph.vertices
vertices.collect.foreach(println)

val edges = graph.edges
edges.collect.foreach(println)
//
val triplets = graph.triplets
triplets.take(3)
triplets.map(t=>t.toString).collect().foreach(println)

// Find out indegrees and outdegrees for each vertex
val inDeg = graph.inDegrees // Followers
inDeg.collect()
val outDeg = graph.outDegrees // Follows
outDeg.collect()
val allDeg = graph.degrees
allDeg.collect()

// Create a subgraph with those edges where the value is greater than 4
val g1 = graph.subgraph(epred = e => e.attr > 4)
g1.triplets.collect.foreach(println)

// Create a subgraph with those vertices where the age is more than 18
val g2 = graph.subgraph(vpred = (id, person) => person.age > 18)
g2.triplets.collect.foreach(println)

// Find the connected components
val cc = graph.connectedComponents().vertices
cc.collect.foreach(println)
// find strongly connected components

val scc = graph.stronglyConnectedComponents(2).vertices
scc.collect.foreach(println)

// find pagerank
val ranks = graph.pageRank(0.0001).vertices
ranks.collect.foreach(println)
