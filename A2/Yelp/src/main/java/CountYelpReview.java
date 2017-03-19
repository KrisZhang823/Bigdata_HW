
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.text.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;



public class CountYelpReview{
	

	public static class ReviewMap extends Mapper<LongWritable, Text, Text, DoubleWritable>{
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			//from reviews
			String delims = "//^";
			String[] reviewData = StringUtils.split(value.toString(),delims);
			if (reviewData.length ==4) {
				try {
					double rating = Double.parseDouble(reviewData[3]);
					context.write(new Text(reviewData[2]), new DoubleWritable(rating));
				}
				catch (NumberFormatException e) {
					context.write(new Text(reviewData[2]), new DoubleWritable(0.0));
				}
			}		
		}
	
		@Override
		protected void setup(Context context)
				throws IOException, InterruptedException {
			
		
		
		}
	}
	
	public static class BusinessMap extends Mapper<LongWritable, Text, Text, IntWritable>{
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			//from business
			String delims = "^";
			//StringTokenizer itr = new StringTokenizer(value.toString(), delims);
			String[] businessData = StringUtils.split(value.toString(),delims);
			//String[] mydata = value.toString().split("\\^");
			if (businessData.length ==3) {
				if(businessData[1].contains("Palo"))
					context.write(new Text(businessData[1]), new IntWritable(1));
			}
			/*if (mydata.length > 3){
				//if("review".compareTo(mydata[3])== 0){
					context.write(new Text(mydata[1]),new IntWritable(1));
				//}
			}*/	
					
		}
	
		@Override
		protected void setup(Context context)
				throws IOException, InterruptedException {
			
		
		
		}
	}

	public static class Reduce extends Reducer<Text,IntWritable,Text,IntWritable> {
		
		public void reduce(Text key, Iterable<IntWritable> values,Context context ) throws IOException, InterruptedException {
		
			int count=0;
			for(IntWritable t : values){
				count++;
			}
			
			context.write(key,new IntWritable(count));
			
		}
	}

	
	public static class ReviewReduce extends Reducer<Text,DoubleWritable,Text,Text> {
		
		private Map<String, Double> countMap = new HashMap<>();
		
		@Override
		public void reduce(Text key, Iterable<DoubleWritable> values,Context context ) throws IOException, InterruptedException {
		
			int count=0;
			double sum = 0.0;
			/*Iterator<IntWritable> itr = values.iterator();
			while(itr.hasNext()){
				count++;
				sum += itr.next().get();
			}*/
			for (DoubleWritable val : values) {
				sum += val.get();
				count++;
			}
			Double avg =  ((double)sum/(double)count);
			NumberFormat formatter = new DecimalFormat("#0.00");  
			//context.write(key,new Text(sum + " " + count + " " + formatter.format(avg)));
			countMap.put(key.toString(), avg);
		}
		
		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException {
			Set<Entry<String, Double>> set = countMap.entrySet();
	        List<Entry<String, Double>> list = new ArrayList<Entry<String, Double>>(set);
	        Collections.sort( list, new Comparator<Map.Entry<String, Double>>()
	        {
	            public int compare( Map.Entry<String, Double> o1, Map.Entry<String, Double> o2 )
	            {
	                return (o2.getValue()).compareTo( o1.getValue() );
	            }
	        } );
	        int counter = 0;
	        
	        for(Map.Entry<String, Double> entry:list){
	            while(counter <10) {
	            	context.write(new Text(entry.getKey()), new Text(entry.getValue().toString()));
	            	counter++;
	            }
	        }
	
	        
		}
	}
	
// Driver program
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();		// get all args
		if (otherArgs.length != 2) {
			System.err.println("Usage: CountYelpReview <in> <out>");
			System.exit(2);
		}
		
		//DistributedCache.addCacheFile(new URI("hdfs://cshadoop1"+ otherArgs[1]), conf);       
		
		//conf.set("movieid", otherArgs[3]);
		 
		  
		Job job = Job.getInstance(conf, "CountYelp");
		job.setJarByClass(CountYelpReview.class);
	   
		job.setMapperClass(ReviewMap.class);
		job.setReducerClass(ReviewReduce.class);
		//job.setNumReduceTasks(0);
		//uncomment the following line to add the Combiner
		//job.setCombinerClass(Reduce.class);
		
		// set output key type 
		job.setOutputKeyClass(Text.class);
		
		
		// set output value type
		job.setMapOutputValueClass(DoubleWritable.class);
		job.setOutputValueClass(Text.class);
		
		
		//set the HDFS path of the input data
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		// set the HDFS path for the output 
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		
		//Wait till job completion
		job.waitForCompletion(true);
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}

	
	