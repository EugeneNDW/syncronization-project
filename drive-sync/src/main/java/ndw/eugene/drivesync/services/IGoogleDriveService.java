package ndw.eugene.drivesync.services;

import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import ndw.eugene.drivesync.dto.FileInfoDto;

import java.io.IOException;
import java.util.List;

public interface IGoogleDriveService {
    File uploadFIle(java.io.File filePath, FileInfoDto fileInfo);
    String deleteFileById(String fileId);
    List<File> showAllFiles();
}