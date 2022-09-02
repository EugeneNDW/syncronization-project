package ndw.eugene.drivesync.services;

import ndw.eugene.drivesync.data.entities.DriveFolder;
import ndw.eugene.drivesync.dto.RenameFolderDto;

import java.io.IOException;

public interface IFolderService {

    DriveFolder getFolderByChatId(long chatId);

    void renameFolder(Long chatId, RenameFolderDto renameFolderDto) throws IOException;
}
