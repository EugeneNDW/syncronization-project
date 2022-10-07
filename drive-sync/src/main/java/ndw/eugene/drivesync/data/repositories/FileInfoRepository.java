package ndw.eugene.drivesync.data.repositories;

import ndw.eugene.drivesync.data.entities.FileInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FileInfoRepository extends CrudRepository<FileInfo, Long> {
    List<FileInfo> findAllByChatIdAndDescription(Long chatId, String description);
}
