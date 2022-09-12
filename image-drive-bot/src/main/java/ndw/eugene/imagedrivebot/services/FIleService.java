package ndw.eugene.imagedrivebot.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.conversation.uploadPhoto.PhotoUploadData;
import ndw.eugene.imagedrivebot.dto.FileInfoDto;
import ndw.eugene.imagedrivebot.dto.RenameFolderDto;
import ndw.eugene.imagedrivebot.exceptions.DriveSyncException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static ndw.eugene.imagedrivebot.configuration.BotConfiguration.RESOURCE_NAME;
import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA;

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
    public void sendFileToDisk(long chatId, File fileToDisk, FileInfoDto fileInfo) {
        FormBodyPart filePart = createFilePart(fileToDisk);
        var body = objectToJSON(fileInfo);
        var entity = MultipartEntityBuilder
                .create()
                .addPart(filePart)
                .addPart("fileInfo", new StringBody(body, ContentType.APPLICATION_JSON))
                .build();

        HttpPost request = new HttpPost(diskUrl + "/" + chatId + "/files");
        request.setEntity(entity);

        makeHttpRequest(request);
    }

    @Override
    public void synchronizeFiles(DriveSyncBot bot, long chatId, long userId, PhotoUploadData photoData) {
        photoData.getDocuments()
                .parallelStream()
                .map(bot::downloadFile)
                .forEach(f ->
                        sendFileToDisk(
                                chatId,
                                f,
                                new FileInfoDto(userId, f.getName(), photoData.getDescription(), RESOURCE_NAME)
                        )
                );
    }

    @Override
    public void renameChatFolder(long chatId, String newFolderName) {
        var body = objectToJSON(new RenameFolderDto(newFolderName));
        var requestEntity = new StringEntity(body, ContentType.APPLICATION_JSON);
        var request = new HttpPost(diskUrl + "/" + chatId + "/folders/name");
        request.setEntity(requestEntity);

        makeHttpRequest(request);
    }

    private FormBodyPart createFilePart(File fileToDisk) {
        var fileUTF8Name = "UTF-8''" + URLEncoder.encode(fileToDisk.getName(), StandardCharsets.UTF_8);
        FileBody fileBody = new FileBody(fileToDisk, MULTIPART_FORM_DATA);
        var ContentDispositionHeader = String.format(
                "form-data; name=\"%s\"; filename=\"%s\"; filename*=\"%s\"",
                "file",
                fileToDisk.getName(),
                fileUTF8Name
        );
        return FormBodyPartBuilder
                .create("File", fileBody)
                .addField("Content-Disposition", ContentDispositionHeader)
                .build();
    }

    private String objectToJSON(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e); //todo custom exception
        }
    }

    private void makeHttpRequest(HttpEntityEnclosingRequestBase request) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpclient.execute(request)) {
                var responseStatus = response.getStatusLine().getStatusCode();
                var reason = response.getStatusLine().getReasonPhrase();
                if (responseStatus >= 500) {
                    throw new DriveSyncException("server error was happened with status code: " + responseStatus + " reason: " + reason);
                }
                System.out.println(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e); //todo custom exception
        }
    }
}
