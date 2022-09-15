package ndw.eugene.imagedrivebot.dto;

import java.io.File;

public record FileDownloadResult(
        String fileName,
        File file,
        boolean successStatus
) {
}
