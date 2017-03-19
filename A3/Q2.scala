var name = args(0)
var tf1 = sc.textFile("/yelp/business/business.csv")
var tf2 = sc.textFile("/yelp/review/review.csv")
var tf3 = sc.textFile("/yelp/user/user.csv")

var r = tf2.map(line => line.split('^')).map(a=> (a(1),a(3).toDouble))
var u = tf3.map(line => line.split('^')).map(a=> (a(0),a(1)))

var avg = r.mapValues((_,1)).reduceByKey((x,y)=>(x._1 + y._1,x._2 + y._2)).mapValues{case(sum,count) => (1.0*sum)/count}
var res = avg.join(u).map(x =>(x._2._2,x._2._1)).filter(x => x._1 == name)
res.map(x => x._2).collect().foreach(println)


