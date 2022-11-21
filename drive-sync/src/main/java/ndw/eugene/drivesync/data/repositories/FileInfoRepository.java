package ndw.eugene.drivesync.data.repositories;

import ndw.eugene.drivesync.data.entities.FileInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileInfoRepository extends CrudRepository<FileInfo, Long> {
    List<FileInfo> findAllByChatIdAndDescription(Long chatId, String description);

    @Query(value = "SELECT * FROM file_info WHERE chat_id = :chatId ORDER BY random() LIMIT 1", nativeQuery = true)
    Optional<FileInfo> findRandomHistoryFile(@Param("chatId") Long chatId);
}
