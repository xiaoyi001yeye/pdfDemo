package ve.pdf.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WordsData {
    private String fileName;

    private Integer page;
    private String index;

    private String type;

    private List<String> words;



}
