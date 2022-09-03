package ndw.eugene.drivesync.services;

import com.google.api.services.drive.model.File;
import ndw.eugene.drivesync.dto.FileInfoDto;

import java.io.IOException;
import java.util.List;

public interface IGoogleDriveService {
    File uploadFIle(long chatId, java.io.File filePath, FileInfoDto fileInfo);
    String deleteFileById(String fileId);
    List<File> showAllFiles();
}
