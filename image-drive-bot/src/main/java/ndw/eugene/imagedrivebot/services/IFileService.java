package ndw.eugene.imagedrivebot.services;

import ndw.eugene.imagedrivebot.dto.FileInfoDto;

import java.io.File;

public interface IFileService {
    void sendFileToDisk(long chatId, File fileToDisk, FileInfoDto fileInfo);

    void renameChatFolder(long chatId, String newFolderName);
}
