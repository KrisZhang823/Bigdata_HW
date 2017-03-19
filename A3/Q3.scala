var tf1 = sc.textFile("/yelp/business/business.csv")
var tf2 = sc.textFile("/yelp/review/review.csv")
var tf3 = sc.textFile("/yelp/user/user.csv")

var b = tf1.map(line => line.split('^')).map(a=> (a(0),(a(0),a(1),a(2)))).filter(x => x._2._2.contains("Stanford"))
var r = tf2.map(line => line.split('^')).map(a=> (a(1),(a(2),a(3))))
var u = tf3.map(line => line.split('^')).map(a=> (a(0),a(1)))

var join1 = r.join(u).map(x => (x._2._1._1,(x._1,x._2._1._2)))
var join2 = join1.join(b).map(x => x._2._1)
join2.takeOrdered(10).foreach(println)


