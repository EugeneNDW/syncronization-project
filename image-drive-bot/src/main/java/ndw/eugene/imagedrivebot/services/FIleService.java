package ndw.eugene.imagedrivebot.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.dto.CreateFolderDto;
import ndw.eugene.imagedrivebot.dto.FileInfoDto;
import ndw.eugene.imagedrivebot.dto.FilesSynchronizationResponse;
import ndw.eugene.imagedrivebot.dto.RenameFolderDto;
import ndw.eugene.imagedrivebot.exceptions.FolderAlreadyExistsException;
import ndw.eugene.imagedrivebot.exceptions.FolderNotFoundException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
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
import org.telegram.telegrambots.meta.api.objects.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static ndw.eugene.imagedrivebot.configurations.BotConfiguration.RESOURCE_NAME;
import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA;

@Service
public class FIleService implements IFileService {

    @Autowired
    private final ObjectMapper objectMapper;

    private final String diskUrl;

    private final String infoPartName = "fileInfo";

    public FIleService(ObjectMapper objectMapper, @Value("${application.driveservice.url}") String diskUrl) {
        this.objectMapper = objectMapper;
        this.diskUrl = diskUrl;
    }

    @Override
    public List<FilesSynchronizationResponse> synchronizeFiles(DriveSyncBot bot, long chatId, long userId, String description, List<Document> documents) {
        return documents
                .parallelStream()
                .map(bot::downloadFile)
                .map(f -> {
                            if (f.successStatus()) {
                                return sendFileToDisk(chatId, f.file(), new FileInfoDto(
                                        userId,
                                        f.fileName(),
                                        description,
                                        RESOURCE_NAME));
                            } else {
                                return new FilesSynchronizationResponse(f.fileName(), false);
                            }
                        }
                ).toList();
    }

    @Override
    public FilesSynchronizationResponse sendFileToDisk(long chatId, File fileToDisk, FileInfoDto fileInfo) {
        FormBodyPart filePart = createFilePart(fileToDisk);
        var body = objectToJSON(fileInfo);
        var entity = MultipartEntityBuilder
                .create()
                .addPart(filePart)
                .addPart(infoPartName, new StringBody(body, ContentType.APPLICATION_JSON))
                .build();

        HttpPost request = new HttpPost(diskUrl + "/" + chatId + "/files");
        request.setEntity(entity);

        var responseResult = makeHttpRequest(request);
        var syncSuccess = responseResult.getStatusLine().getStatusCode() < 400;

        return new FilesSynchronizationResponse(fileInfo.name(), syncSuccess);
    }

    @Override
    public void renameChatFolder(long chatId, String newFolderName) {
        var body = objectToJSON(new RenameFolderDto(newFolderName));
        var requestEntity = new StringEntity(body, ContentType.APPLICATION_JSON);
        var request = new HttpPost(diskUrl + "/" + chatId + "/folders/name");
        request.setEntity(requestEntity);

        var responseResult = makeHttpRequest(request);
        if (responseResult.getStatusLine().getStatusCode() == 404) {
            throw new FolderNotFoundException();
        }
    }

    @Override
    public void createChatFolder(long chatId, String folderName) {
        var body = objectToJSON(new CreateFolderDto(folderName));
        var requestEntity = new StringEntity(body, ContentType.APPLICATION_JSON);
        var request = new HttpPost(diskUrl + "/" + chatId + "/folders");
        request.setEntity(requestEntity);

       var responseResult = makeHttpRequest(request);
       if (responseResult.getStatusLine().getStatusCode() == 409) {
           throw new FolderAlreadyExistsException();
       }
    }

    @Override
    public File searchFile(long chatId, String query) {
        var request = new HttpGet(diskUrl + "/" + chatId + "/files?query=" + query);
        return downloadFile(request);
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

    private File downloadFile(HttpRequestBase request) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpclient.execute(request)) {
                System.out.println(response);
                var entity = response.getEntity();
                InputStream is = entity.getContent();
                var disp = response.getHeaders("Content-Disposition");
                String fileName = disp[0].getElements()[0].getValue();
                var file = new java.io.File(System.getProperty("java.io.tmpdir"), "tmp" + System.currentTimeMillis() + fileName);
                try (FileOutputStream fos = new FileOutputStream(file, false)) {
                    is.transferTo(fos);
                }

                return file;
            }
        } catch (IOException e) {
            throw new RuntimeException(e); //todo custom exception
        }
    }

    private HttpResponse makeHttpRequest(HttpRequestBase request) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpclient.execute(request)) {
                System.out.println(response);
                return response;
            }
        } catch (IOException e) {
            throw new RuntimeException(e); //todo custom exception
        }
    }
}
