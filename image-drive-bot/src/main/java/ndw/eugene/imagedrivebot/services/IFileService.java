package ndw.eugene.imagedrivebot.services;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.conversation.uploadPhoto.PhotoUploadData;
import ndw.eugene.imagedrivebot.dto.FileInfoDto;
import ndw.eugene.imagedrivebot.dto.FilesSynchronizationResponse;

import java.io.File;
import java.util.List;

public interface IFileService {
    FilesSynchronizationResponse sendFileToDisk(long chatId, File fileToDisk, FileInfoDto fileInfo);

    List<FilesSynchronizationResponse> synchronizeFiles(DriveSyncBot bot, long chatId, long userId, PhotoUploadData pd);

    void renameChatFolder(long chatId, String newFolderName);
}
