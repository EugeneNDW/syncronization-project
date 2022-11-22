package ndw.eugene.drivesync.services;

import com.google.api.services.drive.model.File;
import ndw.eugene.drivesync.data.entities.FileInfo;
import ndw.eugene.drivesync.data.repositories.FileInfoRepository;
import ndw.eugene.drivesync.dto.FileInfoDto;
import ndw.eugene.drivesync.exceptions.FileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FileService implements IFileService {

    @Autowired
    private final FileInfoRepository fileInfoRepository;

    @Autowired
    private final IGoogleDriveService googleDriveService;

    @Autowired
    private final IFolderService folderService;

    public FileService(FileInfoRepository fileInfoRepository, IGoogleDriveService googleDriveService, IFolderService folderService) {
        this.fileInfoRepository = fileInfoRepository;
        this.googleDriveService = googleDriveService;
        this.folderService = folderService;
    }

    @Override
    public FileInfo uploadFile(long chatId, java.io.File filePath, FileInfoDto fileInfoDto) {
        var folder = folderService.getFolderByChatId(chatId);
        var driveFile = googleDriveService.uploadFIle(folder.getFolderId(), filePath, fileInfoDto);
        var fileInfo = createFileInfo(chatId, fileInfoDto, driveFile);
        return saveFileInfo(fileInfo);
    }

    @Override
    public FileInfo saveFileInfo(FileInfo fileInfo) {
        return fileInfoRepository.save(fileInfo);
    }

    @Override
    public java.io.File searchFile(Long chatId, String query) {
        if (query.equals("history")) {
            var fileInfo = getRandomHistoryFile(chatId);
            var file = googleDriveService.getFileById(fileInfo.getFileId(), fileInfo.getName());

            return file;
        }

        throw new IllegalArgumentException("service doesn't support this query yet!");
    }

    private FileInfo getRandomHistoryFile(Long chatId) {
        var file = fileInfoRepository.findRandomHistoryFile(chatId);
        if (file.isEmpty()) {
            throw new FileNotFoundException("history");
        }

        return file.get();
    }

    private FileInfo createFileInfo(long chatId, FileInfoDto fileInfoDto, File gdFile) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(fileInfoDto.name());
        fileInfo.setDescription(fileInfoDto.description());
        fileInfo.setSource(fileInfoDto.resource());
        fileInfo.setFileId(gdFile.getId());
        fileInfo.setUserId(fileInfoDto.userId());
        fileInfo.setChatId(chatId);
        fileInfo.setHistory(fileInfoDto.isHistory());

        return fileInfo;
    }
}
