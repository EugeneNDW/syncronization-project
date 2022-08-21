package ndw.eugene.drivesync.data.repositories;

import ndw.eugene.drivesync.data.entities.DriveFolder;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DriveFolderRepository extends CrudRepository<DriveFolder, Long> {
    Optional<DriveFolder> findByChatId(Long chatId);
}
