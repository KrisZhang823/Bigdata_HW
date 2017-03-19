package WordCount.WordCount;

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
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Progressable;

public class Part1 {
	
	public static void uploadText(String uri,String fileName) {
		String dst = "hdfs://cshadoop1/user/yxz154530/assignment2/" + fileName;
        
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
	
	
	
	public static class MyMapper extends Mapper<Object, Text, Text, IntWritable>{
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();
		public static Set<String> positives;
		public static Set<String> negatives;
		
		public void map(Object key, Text value,Context context) throws IOException, InterruptedException{
			init();
			StringTokenizer itr = new StringTokenizer(value.toString().toLowerCase());
			
			while(itr.hasMoreTokens()){
				String token = itr.nextToken().trim();
				if(positives.contains(token)){
					word.set("positive");
					context.write(word, one);
				}else if(negatives.contains(token)){
					word.set("negative");
					context.write(word, one);
				}
			}
			
			
		}
	}
	
	public static class MyReducer extends Reducer<Text,IntWritable,Text,IntWritable>{
		private IntWritable result = new IntWritable();
		private static int positiveCount = 0;
		private static int negativeCount = 0;
		public void reduce(Text key, Iterable<IntWritable> values,Context context) 
					throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
			 sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}
	
	  public static void main(String[] args) throws Exception {
		    Configuration conf = new Configuration();
		    conf.set("mapred.job.tracker", "hdfs://cshadoop1:61120");
		    conf.set("yarn.resourcemanager.address", "cshadoop1.utdallas.edu:8032");
		    conf.set("mapreduce.framework.name", "yarn");
		    Job job = Job.getInstance(conf, "word count");
		    job.setJarByClass(Part1.class);
		    job.setMapperClass(MyMapper.class);
		    job.setCombinerClass(MyReducer.class);
		    job.setReducerClass(MyReducer.class);
		    job.setOutputKeyClass(Text.class);
		    job.setOutputValueClass(IntWritable.class);
		    FileInputFormat.addInputPath(job, new Path(args[0]));
		    FileOutputFormat.setOutputPath(job, new Path(args[1]));
		    System.exit(job.waitForCompletion(true) ? 0 : 1);
	  }
	  
	  public static void init(){
		  if(MyMapper.positives != null)return;
		  MyMapper.positives = new HashSet<String>();
		  MyMapper.negatives = new HashSet<String>();
		  String negativeFile = System.getProperty("user.dir") + File.separator + "negative.txt";
		  String positiveFile = System.getProperty("user.dir") + File.separator + "positive.txt";
		  
		  uploadText(positiveFile,"positive.txt");
		  uploadText(negativeFile,"negative.txt");
		  
		  readFile("positive.txt",1);
		  readFile("negative.txt",2);  
	  }
	  
	  public static void readFile(String fileName,int type){
		  try{
	            Path pt=new Path("hdfs://cshadoop1/user/yxz154530/assignment2/"+fileName);//Location of file in HDFS
	            FileSystem fs = FileSystem.get(new Configuration());
	            BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(pt)));
	            String line;
	            line=br.readLine();
	            while (line != null){
	                if(type == 1){
	                	MyMapper.positives.add(line.trim());
	                }else{
	                	MyMapper.negatives.add(line.trim());
	                }
	           
	                line=br.readLine();
	            }
	        }catch(Exception e){
	      }
	  }
	
	

}
