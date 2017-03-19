import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class test {
   public static void main(String[] args) {
       try {
           Scanner sc = new Scanner(new File("moby.txt"));
           int i = 0;
           char d = (char)215;
           System.out.print("Hello");
           while (sc.hasNextLine() && i < 5) {
               String line = sc.nextLine();
               String[] words = line.split("×");
               System.out.println(line);
               System.out.print(words[0]);
               System.out.print(", ");
               System.out.print(words[1]);
               System.out.println();
           }
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       }
   }
}