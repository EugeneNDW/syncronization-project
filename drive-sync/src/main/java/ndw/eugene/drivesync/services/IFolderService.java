package ndw.eugene.drivesync.services;

import ndw.eugene.drivesync.data.entities.DriveFolder;
import ndw.eugene.drivesync.dto.CreateFolderDto;
import ndw.eugene.drivesync.dto.RenameFolderDto;

public interface IFolderService {

    DriveFolder getFolderByChatId(long chatId);

    void renameFolder(Long chatId, RenameFolderDto renameFolderDto);

    DriveFolder createFolder(long chatId, CreateFolderDto createFolderDto);
}
