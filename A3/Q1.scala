var tf1 = sc.textFile("/yelp/business/business.csv")
var tf2 = sc.textFile("/yelp/review/review.csv")
var tf3 = sc.textFile("/yelp/user/user.csv")
var b = tf1.map(line => line.split('^')).map(a=> (a(0),(a(0),a(1),a(2))))
var r = tf2.map(line => line.split('^')).map(a=> (a(2),a(3).toDouble))
var rm = r.mapValues((_,1))
var avg1 = rm.reduceByKey((x,y)=>(x._1 + y._1,x._2 + y._2))
var avg2 = avg1.mapValues{case(sum,count) => (1.0*sum)/count}
var reducejoin = avg2.join(b)
var res = reducejoin.map(x =>(x._2._2,x._2._1))
var top10 = res.takeOrdered(10)(Ordering[Double].reverse.on(x => x._2)).foreach(println)


