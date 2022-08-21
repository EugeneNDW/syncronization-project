package ndw.eugene.drivesync.services;

import ndw.eugene.drivesync.data.entities.DriveFolder;

public interface IFolderService {
    DriveFolder getFolderByChatId(long chatId);
}
