
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

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
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;



public class Yelptest2{
	

	public static class Map1 extends Mapper<LongWritable, Text, Text, Text>{
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			//from ratings
			
			String[] mydata = value.toString().split("::");
			if (mydata.length > 23){
				if("review".compareTo(mydata[22])== 0){
					context.write(new Text(mydata[2]),new Text("rev::"+mydata[20]));
				}
			}	
					
		}
	
		@Override
		protected void setup(Context context)
				throws IOException, InterruptedException {
			
		
		
		}
	}
	
	public static class Map2 extends Mapper<LongWritable, Text, Text, Text>{
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			//from ratings
			
			String[] mydata = value.toString().split("::");
			if (mydata.length > 23){
				if("business".compareTo(mydata[22])== 0){
					if(mydata[3].contains("Palo")== true){
					context.write(new Text(mydata[2]),new Text("bus::"+mydata[10]+ " " +mydata[3]));
					}
				}
			}	
					
		}
	
		@Override
		protected void setup(Context context)
				throws IOException, InterruptedException {
			
		
		
		}
	}

	//The reducer class	
		public static class Reduce extends Reducer<Text,Text,Text,Text> {
			private Text result = new Text();
			private Text myKey = new Text();
			//note you can create a list here to store the values
			
			public void reduce(Text key, Iterable<Text> values,Context context ) throws IOException, InterruptedException {


				for (Text val : values) {
					
					result.set(val.toString());
					myKey.set(key.toString());
					context.write(myKey,result );
				

				}



			}		
		}
// Driver program
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();		// get all args
		if (otherArgs.length < 2) {
			System.err.println("Usage: Yelptest2 <in> <out>");
			System.exit(2);
		}
		
		
		Job job = new Job(conf, "JoinYelp");
		job.setJarByClass(Yelptest2.class);
		
		
	   
		//job.setMapperClass(Map.class);
		MultipleInputs.addInputPath(job, new Path(otherArgs[0]), TextInputFormat.class ,
				Map1.class );

		MultipleInputs.addInputPath(job, new Path(otherArgs[1]),TextInputFormat.class,Map2.class );

		job.setReducerClass(Reduce.class);
	//	job.setNumReduceTasks(0);
//		uncomment the following line to add the Combiner
//		job.setCombinerClass(Reduce.class);
		
		// set output key type 
		job.setOutputKeyClass(Text.class);
		// set output value type
		job.setOutputValueClass(Text.class);
		
		//set the HDFS path of the input data
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		// set the HDFS path for the output 
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[2]));
		
		//Wait till job completion
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}

	
	