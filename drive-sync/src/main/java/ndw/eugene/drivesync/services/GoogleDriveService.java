package ndw.eugene.drivesync.services;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import ndw.eugene.drivesync.dto.FileInfoDto;
import ndw.eugene.drivesync.exceptions.DriveException;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;

@Service
@Transactional
public class GoogleDriveService implements IGoogleDriveService {

    public final static String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

    @Autowired
    private final Drive drive;

    @Autowired
    private final Tika tika;

    public GoogleDriveService(final Drive drive, final Tika tika) {
        this.drive = drive;
        this.tika = tika;
    }

    @Override
    public File uploadFIle(String folderId, java.io.File filePath, FileInfoDto fileInfoDto) {
        var metadata = createMetadataFromFileInfo(filePath, fileInfoDto, folderId);
        var mediaContent = fileToFileContent(filePath);
        try {
            return drive.files()
                    .create(metadata, mediaContent)
                    .setFields("id") //todo вынести используемые поля из драйва в ENUM
                    .execute();
        } catch (IOException e) {
            throw new DriveException(e);
        }
    }

    @Override
    public java.io.File getFileById(String fileId, String fileName) {
        try {
            var convFile = new java.io.File(System.getProperty("java.io.tmpdir"), fileName); //todo может быть есть варианты генерации имени лучше.
            try (var fileOutputStream = new FileOutputStream(convFile)) {
                drive.files().get(fileId).executeMediaAndDownloadTo(fileOutputStream);
                return convFile;
            }
        } catch (IOException e) {
            throw new DriveException(e);
        }
    }

    @Override
    public String createFolder(String name) {
        File folder = new File();
        folder.setName(name);
        folder.setMimeType(FOLDER_MIME_TYPE);

        try {
            var createdFolder = drive.files().create(folder).execute();
            return createdFolder.getId();
        } catch (IOException e) {
            throw new DriveException(e);
        }
    }

    @Override
    public void renameFolder(String folderId, String newName) {
        File file = new File().setName(newName);
        try {
            drive.files()
                    .update(folderId, file)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String detectMimeType(java.io.File file) {
        try {
            return tika.detect(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FileContent fileToFileContent(@NonNull java.io.File file) {
        var fileMimeType = detectMimeType(file);
        return new FileContent(fileMimeType, file);
    }

    private File createMetadataFromFileInfo(java.io.File filePath, FileInfoDto fileInfoDto, String folderId) {
        var metadata = new File();
        metadata.setParents(Collections.singletonList(folderId));
        metadata.setName(fileInfoDto.name());
        metadata.setDescription(
                fileInfoDto.name() + " "
                        + fileInfoDto.description() + " "
                        + fileInfoDto.resource());
        return metadata;
    }
}
