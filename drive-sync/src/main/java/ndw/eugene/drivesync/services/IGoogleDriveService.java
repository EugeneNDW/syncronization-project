package ndw.eugene.drivesync.services;

import com.google.api.services.drive.model.File;
import ndw.eugene.drivesync.dto.FileInfoDto;

public interface IGoogleDriveService {
    File uploadFIle(String folderId, java.io.File filePath, FileInfoDto fileInfo);
    java.io.File getFileById(String fileId);
}
