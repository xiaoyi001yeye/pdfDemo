package ve.pdf.demo;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class TextAnalyzer {

    private static final String FILE_PATH = "/home/weiyi/code/english_out/";
    private static Map<String, WordStatistics> wordMap = new HashedMap();

    public static void analyzeText(String text) {
        Gson gson = new Gson();
        WordsData wordsData = gson.fromJson(text, WordsData.class);
        wordsData.getWords().forEach(word -> {
            WordStatistics stats = wordMap.getOrDefault(word, new WordStatistics(word));
            stats.addOccurrence(wordsData.getIndex() + wordsData.getPage());
            wordMap.put(word, stats);
        });
    }

    public static void printStatistics() {
        List<WordStatistics> wordStatisticsList = new ArrayList<>(wordMap.values());
        wordStatisticsList.sort(Comparator.comparing(WordStatistics::getFirstLetter, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(WordStatistics::getFrequency,Comparator.reverseOrder()));
        wordStatisticsList.forEach(stats -> log.info("{}: {}: {}", stats.getWord(), stats.getFrequency(),stats.getOccurrences()));
        DocxHelper.printStatisticsToWord(wordStatisticsList);
    }

    @SneakyThrows
    public static void main(String[] args) {
        try (Stream<Path> paths = Files.list(Path.of(FILE_PATH))) {

            paths.filter(path -> {
                Path fileNamePath = path.getFileName();
                String fileName = fileNamePath.toString().toUpperCase().split("\\.")[0];
                BookIndexEnum bookIndexEnum = BookIndexEnum.getByIndex(fileName);
                return bookIndexEnum != null;
            }).forEach(TextAnalyzer::processFile);
        }
        printStatistics();

    }

    private static void processFile(Path path) {
        try {
            List<String> stringList = Files.readAllLines(path);
            stringList.forEach(TextAnalyzer::analyzeText);
        } catch (IOException e) {
            log.error("Error processing file: {}", path, e);
            throw new RuntimeException(e);
        }
    }
}
