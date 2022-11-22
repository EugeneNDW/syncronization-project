package ndw.eugene.imagedrivebot.services;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.dto.FileInfoDto;
import ndw.eugene.imagedrivebot.dto.FilesSynchronizationResponse;
import org.telegram.telegrambots.meta.api.objects.Document;

import java.io.File;
import java.util.List;

public interface IFileService {
    FilesSynchronizationResponse sendFileToDisk(long chatId, File fileToDisk, FileInfoDto fileInfo);

    List<FilesSynchronizationResponse> synchronizeFiles(DriveSyncBot bot, long chatId, long userId, String description, boolean isHistory,  List<Document> documents);

    void renameChatFolder(long chatId, String newFolderName);

    void createChatFolder(long chatId, String folderName);

    File searchFile(long chatId, String query);
}
