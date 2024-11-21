package ve.pdf.demo;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class PdfOcrApp {

    private static final String CONFIG_FILE = "config.properties";

    private static final Properties properties = new Properties();

    private static String directoryPath = null;
    private static String tessDataPath = null;
    private static String language = null;

    private static String output = null;

    private static Set<String> blackSet = new HashSet<>();

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        try {
            properties.load(PdfOcrApp.class.getClassLoader().getResourceAsStream(CONFIG_FILE));
            directoryPath = properties.getProperty("pdf.directory.path");
            tessDataPath = properties.getProperty("tess.data.path");
            language = properties.getProperty("tesseract.language");
            output = properties.getProperty("output.path");
            setBlack();
            List<String> dirList = List.of("/home/weiyi/code/english/义务教育教科书·英语（精通）（三年级起点）三年级上册/"
                    , "/home/weiyi/code/english/义务教育教科书·英语（精通）（三年级起点）四年级上册/"
                    , "/home/weiyi/code/english/义务教育教科书·英语（三年级起点）四年级下册/"
                    ,"/home/weiyi/code/english/义务教育教科书·英语（精通）（三年级起点）五年级上册/"
                    ,"/home/weiyi/code/english/义务教育教科书·英语（精通）（三年级起点）六年级上册/"
            );
            for (String dir : dirList) {
                processPdfFilesInDirectory(dir);
            }


        } catch (IOException | TesseractException e) {
            log.error("处理过程中出现错误", e);
        } finally {
            log.info("run " + (System.currentTimeMillis() - start) + "ms");
        }
    }

    public static void setBlack() {

        try {
            // 获取文件的URL
            URL resource = PdfOcrApp.class.getClassLoader().getResource("black.txt");
            if (resource != null) {
                // 将URL转换为Path并读取文件内容
                try (Stream<String> lines = Files.lines(Paths.get(resource.toURI()))) {
                    blackSet = lines.flatMap(line -> Stream.of(line.split(","))).collect(Collectors.toSet());
                } catch (IOException e) {
                    System.err.println("读取文件时发生错误: " + e.getMessage());
                }
            } else {
                System.err.println("无法找到black.txt文件");
            }
        } catch (URISyntaxException e) {
            System.err.println("文件路径转换错误: " + e.getMessage());
        }
        System.out.println(blackSet);
    }

    private static void processPdfFilesInDirectory(String directoryPath) throws IOException, TesseractException {
        Path dir = Paths.get(directoryPath);
        List<WordsData> list = new ArrayList<>();
        if (Files.isDirectory(dir)) {
            try (Stream<Path> paths = Files.walk(dir)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().toLowerCase().endsWith(".pdf"))
                        .forEach(path -> {
                            try {
                                String fileName = path.getFileName().toString();
                                BookIndexEnum bookIndex = BookIndexEnum.getBookIndex(fileName);
                                log.info("正在处理PDF文件: {}", path.toAbsolutePath());
                                String text = extractTextFromPdf(path);
                                log.info("提取的单词: {}", text);
                                List<String> wordList = extractWordsFromText(text);
                                log.info("处理后的单词: {}", wordList);
                                String baseFileName = fileName.substring(0, fileName.lastIndexOf('.'));
                                // 按照下划线分割获取最后一部分作为页码
                                String pageStr = baseFileName.split("_")[1];
                                int page = Integer.parseInt(pageStr);
                                list.add(WordsData.builder()
                                        .fileName(bookIndex.getBookName())
                                        .index(bookIndex.getIndex())
                                        .words(wordList)
                                        .page(page)
                                        .type("pdfbox")
                                        .build());
                                /*String text2 = extractTextFromOcr(path);
                                List<String> wordList2 = extractWordsFromText(text2);
                                log.info("OCR处理后的单词: {}", wordList2);
                                list.add(WordsData.builder()
                                        .fileName(bookIndex.getBookName())
                                        .index(bookIndex.getIndex())
                                        .words(wordList2)
                                        .page(page)
                                        .type("ocr")
                                        .build());*/
                            } catch (Exception e) {
                                log.error("处理PDF文件 {} 时出错", path.toAbsolutePath(), e);
                            }
                        });
            }
        } else {
            throw new IllegalArgumentException("给定的路径不是一个目录: " + directoryPath);
        }
        Collections.sort(list, Comparator.comparing(WordsData::getIndex)
                .thenComparing(WordsData::getPage).thenComparing(WordsData::getType));
        writeToJsonLinesFile(list, output);

    }

    private static String extractTextFromOcr(Path path) {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage bufferedImage = renderer.renderImageWithDPI(0, 300);
            Tesseract instance = new Tesseract();
            instance.setDatapath(tessDataPath);
            instance.setLanguage(language);
            String ocrText = instance.doOCR(bufferedImage);
            log.info("ocr text:{}", ocrText);
            return ocrText;
        } catch (Exception e) {
            log.error("处理OCR文件 {} 时出错", path.toAbsolutePath(), e);
            return null;
        }
    }

    private static String extractTextFromPdf(Path path) throws IOException {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    static Pattern wordPattern = Pattern.compile("[a-zA-Z]+['’a-zA-Z]+\\b");

    private static List<String> extractWordsFromText(String text) {
        Matcher matcher = wordPattern.matcher(text);
        List<String> wordList = new ArrayList<>();
        while (matcher.find()) {
            String word = matcher.group();
            if (!blackSet.contains(word)) {
                wordList.add(word);
            }
        }
        return wordList;
    }

    private static List<String> extractWordsFromText(String text, String language, String tessDataPath) throws TesseractException {
        Tesseract instance = new Tesseract();
        instance.setDatapath(tessDataPath);
        instance.setLanguage(language);
        String ocrText = instance.doOCR(new File(text));
        Pattern wordPattern = Pattern.compile("[a-zA-Z]+['’a-zA-Z]+\\b");
        Matcher matcher = wordPattern.matcher(ocrText);
        List<String> wordList = new ArrayList<>();
        while (matcher.find()) {
            String word = matcher.group();
            wordList.add(word);
        }
        return wordList;
    }

    public static void writeToJsonLinesFile(List<WordsData> wordsDataList, String parentPath) throws IOException {
        Gson gson = new Gson();
        Path pathParent = Path.of(parentPath);
        if (!Files.exists(pathParent)) {
            Files.createDirectories(pathParent);
        }
        BookIndexEnum bookIndex = BookIndexEnum.getBookIndex(wordsDataList.get(0).getFileName());
        Path path = Path.of(pathParent.toAbsolutePath().toString(), bookIndex.getIndex() + ".jsonl");
        Files.deleteIfExists(path);
        Files.createFile(path);

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (WordsData data : wordsDataList) {
                String json = gson.toJson(data);
                writer.write(json);
                writer.newLine();
            }
        }
    }
}
