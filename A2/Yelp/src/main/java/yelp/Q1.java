package yelp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
//import java.nio.file.FileSystem;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import org.apache.hadoop.util.Progressable;

public class Q1 {
public static class BusinessMap extends Mapper<LongWritable, Text, Text, IntWritable>{
		
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			//from business
			String delims = "^";
			String[] businessData = StringUtils.split(value.toString(),delims);
			
			if (businessData.length ==3) {
				if(businessData[1].contains("Palo Alto"))
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

	public static void uploadText(String uri,String fileName) {
		String dst = "hdfs://cshadoop1/user/yxz154530/" + fileName;
        
        InputStream in;
		try {
			in = new BufferedInputStream(new FileInputStream(uri));
			Configuration conf = new Configuration();
	        conf.addResource(new Path("/usr/local/hadoop-2.4.1/etc/hadoop/core-site.xml"));
	        conf.addResource(new Path("/usr/local/hadoop-2.4.1/etc/hadoop/hdfs-site.xml"));
	        
	        FileSystem fs = FileSystem.get(URI.create(dst), conf);
	        OutputStream out = fs.create(new Path(dst), new Progressable() {
	          public void progress() {
	            System.out.print(".");
	          }
	        });
	        
	        IOUtils.copyBytes(in, out, 4096, true); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
// Driver program
	public static void main(String[] args) throws Exception {
//		String file1 = System.getProperty("user.dir") + File.separator + "business.csv";
//		String file2 = System.getProperty("user.dir") + File.separator + "review.csv";
//		String file3 = System.getProperty("user.dir") + File.separator + "user.csv";
//		  
//	   uploadText(file1,"yelp/business/business.csv");
//	   uploadText(file2,"review.csv");
//	   uploadText(file1,"user.csv");
		
		
		
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();		// get all args
		if (otherArgs.length != 2) {
			System.err.println("Usage: CountYelpBusiness <in> <out>");
			System.exit(2);
		}
			  
		Job job = Job.getInstance(conf, "CountYelp");
		job.setJarByClass(Q1.class);
	   
		job.setMapperClass(BusinessMap.class);
		job.setReducerClass(Reduce.class);
		//uncomment the following line to add the Combiner
		//job.setCombinerClass(Reduce.class);
		
		// set output key type 
		
		job.setOutputKeyClass(Text.class);
		
		
		// set output value type
		job.setMapOutputValueClass(IntWritable.class);
		job.setOutputValueClass(IntWritable.class);
		
		
		//set the HDFS path of the input data
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		// set the HDFS path for the output 
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		
		//Wait till job completion
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
