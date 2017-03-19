package WordCount.WordCount;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection; 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import WordCount.WordCount.Part1.MyMapper;
 
public class Part2 extends Configured implements Tool {
    private static final Logger LOG = Logger.getLogger(Part2.class);
     
    public static void main(String[] args) throws Exception {

        int res = ToolRunner.run(new Part2(), args);
        System.exit(res);
    }
     
    public int run(String[] args) throws Exception {
        Job job = Job.getInstance(getConf(), "wordcount");
        job.setJarByClass(this.getClass());
        // Use TextInputFormat, the default unless job.setInputFormatClass is used
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.setMapperClass(Map.class);
//      job.setCombinerClass(Reduce.class);    //!!!!!!!!!!!!!!
        job.setReducerClass(Reduce.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(Text.class);
        return job.waitForCompletion(true) ? 0 : 1;
    }
     
    public static class Map extends Mapper<LongWritable, Text, IntWritable, Text> {
//      private final static IntWritable one = new IntWritable(1);
        private String input;
        private boolean caseSensitive = false;
        private static final Pattern WORD_BOUNDARY = Pattern.compile("\\s*\\b\\s*");
        private static Set<String> nouns;
        private static Set<String> pronoun;
        private static Set<String> verb;
        private static Set<String> adverb;
        private static Set<String> adjective;
        private static Set<String> conjunction;
        private static Set<String> preposition;
        private static Set<String> interjection;
        
        protected void setup(Mapper.Context context) throws IOException, InterruptedException {
            if (context.getInputSplit() instanceof FileSplit) {    //???????????????
                this.input = ((FileSplit) context.getInputSplit()).getPath().toString();
            } else {
                this.input = context.getInputSplit().toString();
            }
            Configuration config = context.getConfiguration();
            this.caseSensitive = config.getBoolean("wordcount.case.sensitive", false);
            
            nouns = new HashSet<String>();
            pronoun = new HashSet<String>();
            verb = new HashSet<String>();
            adverb = new HashSet<String>();
            adjective = new HashSet<String>();
            conjunction = new HashSet<String>();
            preposition = new HashSet<String>();
            interjection = new HashSet<String>();
            String file = System.getProperty("user.dir") + File.separator + "mobyposi.txt";
            uploadText(file,"mobyposi.txt");
            readFile("positive.txt");
            
        }
        
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
        
        public static void readFile(String fileName){
  		  try{
  	            Path pt=new Path("hdfs://cshadoop1/user/yxz154530/assignment2/"+fileName);//Location of file in HDFS
  	            FileSystem fs = FileSystem.get(new Configuration());
  	            BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(pt)));
  	            String line;
  	            line=br.readLine();
  	            line = line.trim();
  	            while (line != null){
  	            	String[] word = line.split("×");
  	                for(char c : word[1].toCharArray()){
  	                	if(c == 'N'){nouns.add(word[0]);}
  	                	else if(c == 'p'){nouns.add(word[0]);}
  	                	else if(c == 'h'){nouns.add(word[0]);}
  	                	else if(c == 'V'){verb.add(word[0]);}
  	                	else if(c == 't'){verb.add(word[0]);}
  	                	else if(c == 'i'){verb.add(word[0]);}
  	                	else if(c == 'A'){adjective.add(word[0]);}
  	                	else if(c == 'v'){adverb.add(word[0]);}
  	                	else if(c == 'C'){conjunction.add(word[0]);}
  	                	else if(c == 'P'){preposition.add(word[0]);}
  	                	else if(c == '!'){interjection.add(word[0]);}
  	                }
  	                line=br.readLine();
  	            }
  	        }catch(Exception e){
  	      }
  	  }
 
      public void map(LongWritable offset, Text lineText, Context context)
            throws IOException, InterruptedException {
          String line = lineText.toString();
          if (!caseSensitive) {
            line = line.toLowerCase();
          }
          Text currPOS = new Text();
          IntWritable currLen;
          for (String word : WORD_BOUNDARY.split(line)) {
              if (word.isEmpty() || word.length() < 5) {
                  continue;
              }
              currLen = new IntWritable(word.length());
              String pos = getPosOfWord(word);
                 //!!!!!!!!!!!!!!!!!
              if(isPalindrome(word))
            	  pos += "*y";
              else
            	  pos += "*n";
              
              currPOS.set(pos); 
              context.write(currLen, currPOS);        
         }
      }
      
        private boolean isPalindrome(String s) {
            String reverse = new StringBuffer(s).reverse().toString();
            return s.equals(reverse);
        }
         
        private String getPosOfWord(String word) {
        	String type = "";
        	if(nouns.contains(word))
        		type = "noun";
        	else if(pronoun.contains(word))
        		type = "pronoun";
        	else if(verb.contains(word))
        		type = "verb";
        	else if(adverb.contains(word))
        		type = "adverb";
        	else if(adjective.contains(word))
        		type = "adjective";
        	else if(conjunction.contains(word))
        		type = "conjunction";
        	else if(preposition.contains(word))
        		type = "preposition";
        	else if(interjection.contains(word))
        		type = "interjection";
        	else
        		type = "other";
        	
        	
//            String[] posList = new String[]{"noun", "pronoun", "verb", "adverb", "adjective", 
//            		"conjunction", "preposition", "interjection"};
//            Random random = new Random();
//            return posList[random.nextInt(8)];
            
            return type;
        }
    }
 
      public static class Reduce extends Reducer<IntWritable, Text, IntWritable, Text> {
          private boolean isPalindrome(String s) {
                String reverse = new StringBuffer(s).reverse().toString();
                return s.equals(reverse);
            }
           
        @Override
        public void reduce(IntWritable len, Iterable<Text> poss, Context context)
            throws IOException, InterruptedException {
            Text summary = new Text();
            HashMap<String, Integer> map = new HashMap<String, Integer>();         
            int sum = 0;
            int palindromes = 0;
            String[] posStr;
            for (Text pos : poss) {
                sum++;
                posStr = pos.toString().split("*");
                if (map.containsKey(posStr)) {
                    map.put(posStr[0], map.get(posStr[0]) + 1);
                } else {
                    map.put(posStr[0], 1);
                }
                if(posStr[1].equals("y"))
                	palindromes++;
            }
            StringBuilder summarySb = new StringBuilder("Length: " + len + "\n" + "Count of Words: " + sum + "\n");
            summarySb.append("Distribution of POS: {");
            for (Entry<String, Integer> entry: map.entrySet()) {
                summarySb.append(entry.getKey() + ": " + entry.getValue() + "; ");
            }
            summarySb.append("}\n");
            // add palindromes information
            summarySb.append("Number of palindromes: " + palindromes + "\n");
            summary.set(summarySb.toString());
            context.write(len, summary);
        }
      }
}
