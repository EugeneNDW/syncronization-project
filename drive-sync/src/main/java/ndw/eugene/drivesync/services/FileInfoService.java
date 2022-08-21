package ndw.eugene.drivesync.services;

import ndw.eugene.drivesync.data.entities.FileInfo;
import ndw.eugene.drivesync.data.repositories.FileInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileInfoService implements IFileInfoService{

    @Autowired
    private final FileInfoRepository fileInfoRepository;

    public FileInfoService(FileInfoRepository fileInfoRepository) {
        this.fileInfoRepository = fileInfoRepository;
    }

    @Override
    public void saveFileInfo(FileInfo fileInfo) {
        fileInfoRepository.save(fileInfo);
    }
}
