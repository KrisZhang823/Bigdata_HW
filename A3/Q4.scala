var tf1 = sc.textFile("/yelp/business/business.csv")
var tf2 = sc.textFile("/yelp/review/review.csv")
var tf3 = sc.textFile("/yelp/user/user.csv")

var r = tf2.map(line => line.split('^')).map(a=> (a(1),1)).reduceByKey(_+_)
var u = tf3.map(line => line.split('^')).map(a=> (a(0),a(1)))

var res = u.join(r).map(x => (x._2._2,(x._1,x._2._1))).takeOrdered(5)(Ordering[Int].reverse.on(x => x._1)).map(x => x._2).foreach(println)