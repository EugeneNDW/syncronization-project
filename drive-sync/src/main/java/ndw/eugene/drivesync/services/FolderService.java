package ndw.eugene.drivesync.services;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import ndw.eugene.drivesync.data.entities.DriveFolder;
import ndw.eugene.drivesync.data.repositories.DriveFolderRepository;
import ndw.eugene.drivesync.dto.RenameFolderDto;
import ndw.eugene.drivesync.exceptions.DriveException;
import ndw.eugene.drivesync.exceptions.FolderNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;

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
        var chatFolder = driveFolderRepository.findByChatId(chatId);
        DriveFolder folder;
        if (chatFolder.isEmpty()) {
            var folderId = createFolder(chatId + "");

            folder = new DriveFolder();
            folder.setTitle(chatId + "");
            folder.setFolderId(folderId);
            folder.setChatId(chatId);

            return driveFolderRepository.save(folder);
        } else {
            return chatFolder.get();
        }
    }

    @Override
    public void renameFolder(Long chatId, RenameFolderDto renameFolderDto) throws IOException {
        var folder = driveFolderRepository.findByChatId(chatId);
        if (folder.isPresent()) {
            folder.get().setTitle(renameFolderDto.newName());
            String folderId = folder.get().getFolderId();
            File file = new File().setName(renameFolderDto.newName());
            drive.files()
                    .update(folderId, file)
                    .execute();
        } else {
            throw new FolderNotFoundException(chatId);
        }
    }

    private String createFolder(String name) {
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
