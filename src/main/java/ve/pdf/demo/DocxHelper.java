package ve.pdf.demo;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class DocxHelper {

    public static void printStatisticsToWord(List<WordStatistics> wordStatisticsList) {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph paragraph;
            XWPFTable table = doc.createTable(1,4);

            XWPFTableRow tableRow = table.getRow(0);
            tableRow.getCell(0).setText("Word");
            tableRow.getCell(1).setText("Frequency");
            tableRow.getCell(2).setText("Word");
            tableRow.getCell(3).setText("Frequency");

            int rowNum = 0;
            for (int i = 0, wordStatisticsListSize = wordStatisticsList.size(); i < wordStatisticsListSize; i++) {
                WordStatistics stats = wordStatisticsList.get(i);
                if(i%2 == 0){
                    tableRow = table.createRow();
                    rowNum++;
                    tableRow.getCell(0).setText(stats.getWord());
                    tableRow.getCell(1).setText(String.valueOf(stats.getFrequency()));
                }else {
                    tableRow = table.getRow(rowNum);
                    tableRow.getCell(2).setText(stats.getWord());
                    tableRow.getCell(3).setText(String.valueOf(stats.getFrequency()));
                }


            }

            // 将文档写入文件
            try (FileOutputStream out = new FileOutputStream("WordStatistics.docx")) {
                doc.write(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
