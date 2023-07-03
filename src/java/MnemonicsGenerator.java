import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MnemonicsGenerator {

    private final Map<String, ArrayList<String>> KEYPAD = Stream.of(new Object[][] {
            {"1", new ArrayList<>(Arrays.asList("1"))},
            {"2", new ArrayList<>(Arrays.asList("A", "B", "C"))},
            {"3", new ArrayList<>(Arrays.asList("D", "E", "F"))},
            {"4", new ArrayList<>(Arrays.asList("G", "H", "I"))},
            {"5", new ArrayList<>(Arrays.asList("J", "K", "L"))},
            {"6", new ArrayList<>(Arrays.asList("M", "N", "O"))},
            {"7", new ArrayList<>(Arrays.asList("P", "Q", "R", "S"))},
            {"8", new ArrayList<>(Arrays.asList("T", "U", "V"))},
            {"9", new ArrayList<>(Arrays.asList("W", "X", "Y", "Z"))},
            {"0", new ArrayList<>(Arrays.asList("0"))}
    }).collect(Collectors.toMap(d -> (String) d[0], d -> (ArrayList<String>) d[1]));

    private final ArrayList<String> wordbank;
    private int minWordLen;   // minWordLen for every word in a number sequence (each subsequence separated by 0/1), if possible
    // "possible" meaning: the length of consecutive number sequence without 0/1 >= minWordLen

    // constructor
    public MnemonicsGenerator(int minWordLen, String wordBankPath) throws FileNotFoundException {
        this.minWordLen = minWordLen;

        Scanner scan = new Scanner(new File(wordBankPath));
        wordbank = new ArrayList<>();
        while (scan.hasNext()){
            wordbank.add(scan.next().toUpperCase());
//            System.out.println(wordbank.get(wordbank.size()-1));
        }
        scan.close();
    }

    // getter
    public int getMinWordLen() {
        return minWordLen;
    }

    public ArrayList<String> getWordbank() {
        return wordbank;
    }

    // setter
    public void setMinWordLen(int minWordLen) {
        this.minWordLen = minWordLen;
    }

    // check if real words
    public ArrayList<String> wordsInBank(ArrayList<String> wordsToCheck){
        ArrayList<String> wordsExist = new ArrayList<>();
        for (String word : wordsToCheck) {
//            if (wordbank.parallelStream().anyMatch(s -> s.equals(word.toUpperCase()))) {wordsExist.add(word.toUpperCase());}
            if (wordbank.contains(word.toUpperCase())) {wordsExist.add(word.toUpperCase());}
        }
//        System.out.println("words exist:" + wordsExist);
//        System.out.println(wordbank.contains("BLE"));
        return wordsExist;
    }

    // numeric string to keypad spelling (does not check word existence)
    public ArrayList<String> toSpelling(String number) {
        ArrayList<String> allCombinations = new ArrayList<>();
        if (number.length() == 0);
        else if (number.length() == 1) allCombinations.addAll(KEYPAD.get(number));
        else {
            ArrayList<String> firstNumSpells = KEYPAD.get(number.substring(0,1));
//            System.out.println(firstNumSpells.toString());  // sanity
            ArrayList<String> remainingSpells = toSpelling(number.substring(1));
            for (String firstNumSpell : firstNumSpells) {
                for (String remainingSpell : remainingSpells) {
                    allCombinations.add(firstNumSpell + remainingSpell);
                }
            }
        }
        return allCombinations;
    }

    //  numeric string to keypad word (single word; check word existence)
    public ArrayList<String> toWords(String number) {
        ArrayList<String> wordsToCheck = toSpelling(number);
//        System.out.println("in bank: " + wordsInBank(wordsToCheck));
        return wordsInBank(wordsToCheck);
    }

    // numeric string to *sequence* of keypad words (check word existence), not preserving 0 and 1
    public ArrayList<ArrayList<String>> toWordSequences(String number) {   // each inner ArrayList is 1 possible word sequence

//        System.out.println("========== SEARCHING ... HANG TIGHT ==========");
        ArrayList<ArrayList<String>> allSequences = new ArrayList<>();

        if (number.length() == 0) { allSequences.add(new ArrayList<>()); }

        else if (number.length() <= minWordLen) {      // if the length of number sequence <= minWordLen: generate a single word for the whole sequence
            allSequences.addAll(toWords(number).stream()
                    .map(s -> new ArrayList<>(Arrays.asList(s)))
                    .collect(Collectors.toCollection(ArrayList::new)));  // String -> ArrayList(String)
        }

        else {           // if the length of number sequence > minWordLen: ensure every word in the number sequence > minWordLen
            // case 1: number sequence is divided
            for (int i = minWordLen; i <= number.length() - minWordLen; i++) {
                String part1 = number.substring(0, i);
                ArrayList<String> part1Words = toWords(part1);
//                System.out.println("part1: " + number.substring(0, i) + " words: " + part1Words.toString());
                ArrayList<ArrayList<String>> remainingWordSequences = toWordSequences(number.substring(i));
//                System.out.println("remaining: " + number.substring(i) + " sequences: " + remainingWordSequences.toString());
                for (String word : part1Words) {
                    for (ArrayList<String> sequence : remainingWordSequences) {
                        if (sequence.stream().map(s -> s.length()).min((m, n) -> m - n).orElse(-1) >= minWordLen){
                            ArrayList<String> newSequence = (ArrayList<String>) sequence.clone();   // sequence.add will mutate original sequence & impact next iteration
                            newSequence.add(0, word);
                            allSequences.add(newSequence);
                        }
                    }
                }
            }
            // case 2: number sequence is not divided
            for (String word : toWords(number)) {
                allSequences.add(new ArrayList<>(Arrays.asList(word)));
            }
        }
        return allSequences;
    }

    // find combinations
    public ArrayList<ArrayList<String>> findCombinations(ArrayList<ArrayList<ArrayList<String>>> choicesAllSlots) {
        // choicesAllSlots: each sub-2D-ArrayList<ArrayList<String>> is all the word sequences for one slot;
        //                  each sub-1D-ArrayList<String> inside all the word sequences for one slot is one word sequence for this slot.

        if (choicesAllSlots.size() <= 1) {return choicesAllSlots.get(0);}   // if numSlots <= 1
        else {
            ArrayList<ArrayList<String>> combinationsSlotsFlattened = new ArrayList<>();
            ArrayList<ArrayList<String>> combinationsRemainingSlots =
                    findCombinations(new ArrayList<ArrayList<ArrayList<String>>>(choicesAllSlots.subList(1, choicesAllSlots.size())));
            for (ArrayList<String> sequence : choicesAllSlots.get(0)) {     // for each word sequence (ArrayList<String>) for the first slot
                for (ArrayList<String> combinationRemainingSlots : combinationsRemainingSlots) {
                    ArrayList<String> sequenceCopy = (ArrayList<String>) sequence.clone(); // cannot directly mutate sequence
                    sequenceCopy.addAll(combinationRemainingSlots);
                    combinationsSlotsFlattened.add(sequenceCopy);
                }
            }
            return combinationsSlotsFlattened;
        }
    }

    // numeric string to sequence of keypad words (check word existence), preserving 0 and 1
    public ArrayList<ArrayList<String>> toWord01Sequences(String number) {
        number = number.replaceAll("\\D+","");   // remove non-digits
        System.out.println("========== SEARCHING ... HANG TIGHT ==========");

        ArrayList<String> numberSlots =
                new ArrayList<>(Arrays.asList(number.split("((?<![0|1])(?=[0|1])|(?<=[0|1])(?![0|1]))")));    // split string of numbers into 0/1 digit groups and other digit groups. e.g., 0|986789234|111|97
        ArrayList<ArrayList<ArrayList<String>>> choicesAllSlots = new ArrayList<>();

        for (String numberSlot : numberSlots) {
            if (Pattern.matches("[0|1]*", numberSlot)) {   // if the current numberSlot is a 0/1 slot
                ArrayList<ArrayList<String>> sequence01 = new ArrayList<>();
                sequence01.add(new ArrayList<>(Arrays.asList(numberSlot)));
                choicesAllSlots.add(sequence01);
            }
            else {
//                System.out.println("========== SEARCHING ... HANG TIGHT ==========");
                choicesAllSlots.add(toWordSequences(numberSlot));
//                System.out.println(choicesAllSlots.toString());
            }
        }

        return findCombinations(choicesAllSlots);
    }

}
