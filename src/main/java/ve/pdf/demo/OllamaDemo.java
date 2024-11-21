package ve.pdf.demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

@Slf4j
public class OllamaDemo {

    private static final String OLLAMA_API_URL = "http://127.0.0.1:11434/api/generate";

    public static void main(String[] args) throws IOException {
        File pdfFile = new File("/home/weiyi/code/english/3B/义务教育教科书·英语（三年级起点）三年级下册_6.pdf");
        String text = extractEnglishWordsFromPdf(pdfFile);
        log.info(text);

    }

    public static String extractEnglishWordsFromPdf(File pdfFile) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(OLLAMA_API_URL);
        String json = "{\"model\": \"llama3:8b\", \"prompt\": \"Why is the sky blue?\",\"stream\": false}";
        // 构建多部分请求实体，包含PDF文件
        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", pdfFile, ContentType.APPLICATION_OCTET_STREAM, pdfFile.getName())
                .addTextBody("prompt",json)
                .build();



        httpPost.setHeader("Content-Type", "multipart/form-data");
        httpPost.setEntity(entity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                HttpEntity responseEntity = response.getEntity();
                return EntityUtils.toString(responseEntity);
            } else {
                throw new IOException("请求失败，状态码: " + statusCode);
            }
        }
    }
}
