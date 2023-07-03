//public class Main {
//    public static void main(String[] args) {
//        System.out.println("Hello world!");
//    }
//}

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {

        MnemonicsGenerator myGenerator = new MnemonicsGenerator(1, "src/resources/words.txt");
        String phoneNumber = "263-346-5282";
//        System.out.println(myGenerator.getWordbank());

        myGenerator.setMinWordLen(2);   // minWordLen for every word in a number sequence (each subsequence separated by 0/1), if possible
        // "possible" meaning: the length of consecutive number sequence without 0/1 >= minWordLen
        ArrayList<ArrayList<String>> word01Sequences = myGenerator.toWord01Sequences(phoneNumber);
        System.out.println("RESULTS FOR NUMBER " + phoneNumber + " (" + word01Sequences.size()
                + " in total. With minimum word length of " + myGenerator.getMinWordLen() + " in ALL POSSIBLE subsequences of numbers.)");
        word01Sequences.stream()
                .map(s -> String.join("-", s))
                .forEach(System.out::println);
    }
}
