package ndw.eugene.drivesync.services;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import ndw.eugene.drivesync.data.entities.FileInfo;
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
import java.util.List;

@Service
@Transactional
public class GoogleDriveService implements IGoogleDriveService {
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
        File metadata = createMetadataFromFileInfo(filePath, fileInfoDto, folderId);
        FileContent mediaContent = fileToFileContent(filePath);
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
    public java.io.File getFileById(String fileId) {
        try {
            var convFile = new java.io.File(System.getProperty("java.io.tmpdir"), fileId + System.currentTimeMillis()); //todo может быть есть варианты генерации имени лучше.
            try (var fileOutputStream = new FileOutputStream(convFile)) {
                drive.files().get(fileId).executeMediaAndDownloadTo(fileOutputStream);
                return convFile;
            }
        } catch (IOException e) {
            throw new DriveException(e);
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
        String fileMimeType = detectMimeType(file);
        return new FileContent(fileMimeType, file);
    }

    private File createMetadataFromFileInfo(java.io.File filePath, FileInfoDto fileInfoDto, String folderId) {
        var metadata = new File();
        metadata.setParents(Collections.singletonList(folderId));
        metadata.setName(filePath.getName());
        metadata.setDescription(
                fileInfoDto.name() + " "
                        + fileInfoDto.description() + " "
                        + fileInfoDto.resource());
        return metadata;
    }

//    @Override
//    public String deleteFileById(String fileId) {
//        try {
//            drive.files().delete(fileId).execute();
//            return "success";
//        } catch (IOException e) {
//            throw new DriveException(e);
//        }
//    }

//    @Override
//    public List<File> showAllFiles() {
//        try {
//            FileList result = drive.files().list()
//                    .setPageSize(100)
//                    .setFields("nextPageToken, files(owners, id, name, description, driveId, explicitlyTrashed, fileExtension, isAppAuthorized, kind, modifiedByMe, parents, permissionIds)")
//                    .execute();
//
//            List<File> files = result.getFiles();
//            logFiles(files);
//
//            return files;
//        } catch (IOException e) {
//            throw new DriveException(e);
//        }
//    }

//    private void logFiles(List<File> files) {
//        if (files == null || files.isEmpty()) {
//            System.out.println("No files found.");
//        } else {
//            System.out.println("Files:");
//            for (File file : files) {
//                System.out.printf("%s (%s)\n", file.getName(), file.getId());
//            }
//        }
//    }
}
