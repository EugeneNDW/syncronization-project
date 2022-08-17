package ndw.eugene.imagedrivebot.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ndw.eugene.imagedrivebot.dto.FileInfoDto;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class FIleService implements IFileService {

    @Autowired
    private final ObjectMapper objectMapper;
    private final String diskUrl;

    public FIleService(ObjectMapper objectMapper, @Value("${application.driveservice.url}") String diskUrl) {
        this.objectMapper = objectMapper;
        this.diskUrl = diskUrl;
    }

    @Override
    public void sendFileToDisk(File fileToDisk, FileInfoDto fileInfo) {
        HttpEntity entity;
        try {
            entity = MultipartEntityBuilder
                    .create()
                    .addPart("file", new FileBody(fileToDisk))
                    .addPart("fileInfo", new StringBody(objectMapper.writeValueAsString(fileInfo), ContentType.APPLICATION_JSON))
                    .build();
        } catch (JsonProcessingException e) { //todo exception
            throw new RuntimeException(e);
        }

        HttpPost request = new HttpPost(diskUrl);
        request.setEntity(entity);

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpclient.execute(request)) {
                System.out.println(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e); //todo exception
        }
    }
}
