package ndw.eugene.drivesync.dto;

import java.util.List;

public record FileInfoDto
        (Long userId,
         String name,
         String description,
         String resource) {
}
