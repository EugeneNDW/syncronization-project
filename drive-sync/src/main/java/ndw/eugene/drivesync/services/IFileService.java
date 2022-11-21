package ndw.eugene.drivesync.services;

import ndw.eugene.drivesync.data.entities.FileInfo;
import ndw.eugene.drivesync.dto.FileInfoDto;

import java.io.File;

public interface IFileService {
    FileInfo uploadFile(long chatId, File filePath, FileInfoDto fileInfo);

    FileInfo saveFileInfo(FileInfo fileInfo);

    FileInfo getRandomHistoryFile(Long chatId);

    File searchAnyFile(Long chatId, String query);
}
