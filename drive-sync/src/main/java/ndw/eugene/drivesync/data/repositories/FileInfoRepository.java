package ndw.eugene.drivesync.data.repositories;

import ndw.eugene.drivesync.data.entities.FileInfo;
import org.springframework.data.repository.CrudRepository;

public interface FileInfoRepository extends CrudRepository<FileInfo, Long> {
}
