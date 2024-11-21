package ve.pdf.demo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JsonFormat {

    static String inputFilePath = "5B/text_input.jsonl";

    static String outputFilePath = "/home/weiyi/code/english_out/5B.jsonl";
    public static void main(String[] args) throws URISyntaxException {
        String fileContent = readFileToString(inputFilePath);
        List<String> jsonTextList = new ArrayList<>();
        try {
            StringBuilder jsonLines = new StringBuilder();
            for (int i=0;i<fileContent.length(); i++){
                char charAt = fileContent.charAt(i);
                if (charAt == '}') {
                    jsonLines.append(charAt).append("\n");
                    jsonTextList.add(jsonLines.toString());
                    jsonLines.delete(0, jsonLines.length() - 1);
                }else if(charAt == '\n'){
                    continue;
                }else{
                    jsonLines.append(charAt);
                }
            }
            Path path = Path.of(outputFilePath);
            Files.deleteIfExists(path);
            Files.createFile(path);
            List<WordsData> wordsDataList = new ArrayList<>();
            for (String text : jsonTextList) {
                Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                WordsData wordsData = gson.fromJson(text, WordsData.class);
                BookIndexEnum bookIndex = BookIndexEnum.getBookIndex(wordsData.getFileName());
                wordsData.setFileName(bookIndex.getBookName());
                wordsData.setIndex(bookIndex.getIndex());
                wordsData.setType("llm");
                wordsDataList.add(wordsData);
            }


            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                for (WordsData wordsData : wordsDataList) {
                    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                    String prettyJson = gson.toJson(wordsData);
                    writer.write(prettyJson);
                    writer.newLine();
                }
            }



            System.out.println("转换完成，JSON Lines已保存到: " + outputFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static String readFileToString(String filePath) {
        URL resource = JsonFormat.class.getClassLoader().getResource(filePath);
        if (resource == null) {
            log.error("资源文件未找到: " + filePath);
            throw new IllegalArgumentException("资源文件未找到: " + filePath);
        }
        try {
            List<String> lines = Files.readAllLines(Paths.get(resource.getPath()));
            return String.join("\n", lines);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
