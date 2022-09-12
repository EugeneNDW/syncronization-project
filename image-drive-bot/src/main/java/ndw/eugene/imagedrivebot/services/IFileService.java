package ndw.eugene.imagedrivebot.services;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.conversation.uploadPhoto.PhotoUploadData;
import ndw.eugene.imagedrivebot.dto.FileInfoDto;

import java.io.File;

public interface IFileService {
    void sendFileToDisk(long chatId, File fileToDisk, FileInfoDto fileInfo);

    void synchronizeFiles(DriveSyncBot bot, long chatId, long userId, PhotoUploadData pd);

    void renameChatFolder(long chatId, String newFolderName);
}
