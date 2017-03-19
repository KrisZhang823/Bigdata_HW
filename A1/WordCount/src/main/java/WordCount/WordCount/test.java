package WordCount.WordCount;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class test {
   public static void main(String[] args) {
       try {
           Scanner sc = new Scanner(new File("./data/mobyposi.txt"));
           int i = 0;
           char d = (char)215;
           while (sc.hasNextLine() && i < 5) {
               String line = sc.nextLine();
               String[] words = line.split(String.valueOf(d));
               System.out.println(line);
               System.out.print(words[0]);
               System.out.print(", ");
//             System.out.print(words[1]);
               System.out.println();
           }
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       }
   }
}
