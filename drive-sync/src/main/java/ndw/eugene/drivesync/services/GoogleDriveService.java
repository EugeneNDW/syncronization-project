package ndw.eugene.drivesync.services;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import ndw.eugene.drivesync.dto.FileInfoDto;
import ndw.eugene.drivesync.exceptions.DriveException;
import ndw.eugene.drivesync.exceptions.TikaException;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService implements IGoogleDriveService {
    private static final String DIRECTORY_ID = "1TMpuJgnXmr8tDiUzjlGPopA1xgWd8JX-";

    @Autowired
    private final Drive drive;
    @Autowired
    private final Tika tika;

    public GoogleDriveService(final Drive drive,
                              final Tika tika) {
        this.drive = drive;
        this.tika = tika;
    }

    @Override
    public File uploadFIle(java.io.File filePath, FileInfoDto fileInfoDto) {
        var metadata = new File();
        metadata.setParents(Collections.singletonList(DIRECTORY_ID));
        metadata.setName(filePath.getName());
        metadata.setDescription(fileInfoDto.name() + " " + fileInfoDto.resource());

        FileContent mediaContent = fileToFileContent(filePath);

        try {
            File gdFile = drive.files()
                    .create(metadata, mediaContent)
                    .setFields("id")
                    .execute();

            System.out.println("File ID: " + gdFile.getId());
            return gdFile;
        } catch (IOException e) {
            throw new DriveException(e);
        }
    }

    @Override
    public String deleteFileById(String fileId) {
        try {
            drive.files().delete(fileId).execute();
            return "success";
        } catch (IOException e) {
            throw new DriveException(e);
        }
    }

    @Override
    public List<File> showAllFiles() {
        try {
            FileList result = drive.files().list()
                    .setPageSize(100)
                    .setFields("nextPageToken, files(owners, id, name, description, driveId, explicitlyTrashed, fileExtension, isAppAuthorized, kind, modifiedByMe, parents, permissionIds)")
                    .execute();

            List<File> files = result.getFiles();
            logFiles(files);

            return files;
        } catch (IOException e) {
            throw new DriveException(e);
        }
    }

    private FileContent fileToFileContent(@NonNull java.io.File file) {
        try {
            String fileMimeType = tika.detect(file);
            return new FileContent(fileMimeType, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void logFiles(List<File> files) {
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
    }
}
