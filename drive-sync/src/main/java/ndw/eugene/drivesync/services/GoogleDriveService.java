package ndw.eugene.drivesync.services;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import ndw.eugene.drivesync.data.entities.FileInfo;
import ndw.eugene.drivesync.dto.FileInfoDto;
import ndw.eugene.drivesync.exceptions.DriveException;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class GoogleDriveService implements IGoogleDriveService {
    @Autowired
    private final IFileInfoService fileInfoService;

    @Autowired
    private final IFolderService folderService;

    @Autowired
    private final Drive drive;

    @Autowired
    private final Tika tika;

    public GoogleDriveService(final IFileInfoService fileInfoService,
                              final IFolderService folderService,
                              final Drive drive,
                              final Tika tika) {
        this.fileInfoService = fileInfoService;
        this.folderService = folderService;
        this.drive = drive;
        this.tika = tika;
    }

    @Override
    public File uploadFIle(long chatId, java.io.File filePath, FileInfoDto fileInfoDto) {
        var folderId = folderService.getFolderByChatId(chatId);

        File metadata = createMetadataFromFileInfo(filePath, fileInfoDto, folderId.getFolderId());
        FileContent mediaContent = fileToFileContent(filePath);

        try {
            File gdFile = drive.files()
                    .create(metadata, mediaContent)
                    .setFields("id")
                    .execute();

            System.out.println("File ID: " + gdFile.getId());

            FileInfo fileInfo = createFileInfo(fileInfoDto, gdFile);
            fileInfoService.saveFileInfo(fileInfo);

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

    private FileInfo createFileInfo(FileInfoDto fileInfoDto, File gdFile) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setDescription(fileInfoDto.description());
        fileInfo.setSource(fileInfoDto.resource());
        fileInfo.setFileId(gdFile.getId());
        fileInfo.setUserId(fileInfoDto.userId());
        return fileInfo;
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
}
