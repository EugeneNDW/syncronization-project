package ndw.eugene.drivesync.services;

import com.google.common.base.Throwables;
import ndw.eugene.drivesync.data.entities.DriveFolder;
import ndw.eugene.drivesync.data.repositories.DriveFolderRepository;
import ndw.eugene.drivesync.dto.CreateFolderDto;
import ndw.eugene.drivesync.dto.RenameFolderDto;
import ndw.eugene.drivesync.exceptions.FolderAlreadyExistsException;
import ndw.eugene.drivesync.exceptions.FolderNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.SQLException;

@Service
@Transactional
public class FolderService implements IFolderService {

    @Autowired
    private final DriveFolderRepository driveFolderRepository;

    @Autowired
    private final IGoogleDriveService googleDriveService;

    public FolderService(DriveFolderRepository driveFolderRepository, IGoogleDriveService googleDriveService) {
        this.driveFolderRepository = driveFolderRepository;
        this.googleDriveService = googleDriveService;
    }

    @Override
    public DriveFolder getFolderByChatId(long chatId) {
        var folder = driveFolderRepository.findByChatId(chatId);
        if (folder.isPresent()) {
            return folder.get();
        } else {
            throw new FolderNotFoundException(chatId);
        }
    }

    @Override
    public void renameFolder(Long chatId, RenameFolderDto renameFolderDto) {
        var folder = getFolderByChatId(chatId);
        folder.setTitle(renameFolderDto.newName());

        googleDriveService.renameFolder(folder.getFolderId(), renameFolderDto.newName()); //todo проблемы с транзакциями
    }

    @Override
    public DriveFolder createFolder(long chatId, CreateFolderDto createFolderDto) {
        var folderId = googleDriveService.createFolder(createFolderDto.folderName()); //todo проблемы с транзакциями

        DriveFolder folder = new DriveFolder();
        folder.setTitle(createFolderDto.folderName());
        folder.setFolderId(folderId);
        folder.setChatId(chatId);

        try {
            return driveFolderRepository.save(folder);
        } catch (RuntimeException e) {
            Throwable rootCause = Throwables.getRootCause(e);
            if (rootCause instanceof SQLException) {
                if ("23505".equals(((SQLException) rootCause).getSQLState())) {
                    throw new FolderAlreadyExistsException(chatId);
                }
            }
            throw new RuntimeException(); //todo что кидать?
        }
    }
}
