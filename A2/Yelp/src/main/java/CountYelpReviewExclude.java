
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



public class CountYelpReviewExclude{
	

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
		
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			//from business
			String delims = "^";
			String[] businessData = StringUtils.split(value.toString(),delims);
			
			if (businessData.length ==3) {
				if(businessData[1].contains("Palo"))
					context.write(new Text(businessData[1]), new IntWritable(1));
			}		
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
			context.write(key,new Text(sum + " " + count + " " + formatter.format(avg)));
			
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
			  
		Job job = Job.getInstance(conf, "CountYelp");
		job.setJarByClass(CountYelpReviewExclude.class);
	   
		job.setMapperClass(BusinessMap.class);
		job.setReducerClass(Reduce.class);
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
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}

	
	