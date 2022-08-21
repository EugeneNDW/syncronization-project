package ndw.eugene.drivesync.services;

import ndw.eugene.drivesync.data.entities.FileInfo;

public interface IFileInfoService {
    void saveFileInfo(FileInfo fileInfo);
}
