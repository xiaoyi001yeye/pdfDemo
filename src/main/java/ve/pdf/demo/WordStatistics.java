package ve.pdf.demo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WordStatistics {

    public WordStatistics(String word) {
        this.word = word;
        this.firstLetter = word.substring(0,1);
    }


    private String word;
    private int frequency;
    private String firstLetter;
    private List<String> occurrences;
    private boolean isCapitalized;


    public void addOccurrence(String occurrence) {
        if (this.occurrences == null) {
            this.occurrences = new ArrayList<>();
        }
        this.occurrences.add(occurrence);
        this.frequency++;
    }

}
