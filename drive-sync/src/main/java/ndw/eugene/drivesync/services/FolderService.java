package ndw.eugene.drivesync.services;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.common.base.Throwables;
import ndw.eugene.drivesync.data.entities.DriveFolder;
import ndw.eugene.drivesync.data.repositories.DriveFolderRepository;
import ndw.eugene.drivesync.dto.CreateFolderDto;
import ndw.eugene.drivesync.dto.RenameFolderDto;
import ndw.eugene.drivesync.exceptions.DriveException;
import ndw.eugene.drivesync.exceptions.FolderAlreadyExistsException;
import ndw.eugene.drivesync.exceptions.FolderNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.sql.SQLException;

@Service
@Transactional
public class FolderService implements IFolderService {
    public final static String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

    @Autowired
    private final DriveFolderRepository driveFolderRepository;

    @Autowired
    private final Drive drive;

    public FolderService(DriveFolderRepository driveFolderRepository, Drive drive) {
        this.driveFolderRepository = driveFolderRepository;
        this.drive = drive;
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
    public void renameFolder(Long chatId, RenameFolderDto renameFolderDto) throws IOException {
        var folder = getFolderByChatId(chatId);

        folder.setTitle(renameFolderDto.newName());
        String folderId = folder.getFolderId();
        File file = new File().setName(renameFolderDto.newName());
        drive.files()
                .update(folderId, file)
                .execute();
    }

    @Override
    public DriveFolder createFolder(long chatId, CreateFolderDto createFolderDto) {
        var folderId = createDriveFolder(createFolderDto.folderName());

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

    private String createDriveFolder(String name) {
        File folder = new File();
        folder.setName(name);
        folder.setMimeType(FOLDER_MIME_TYPE);

        try {
            var createdFolder = drive.files().create(folder).execute();
            return createdFolder.getId();
        } catch (IOException e) {
            throw new DriveException(e);
        }
    }
}
