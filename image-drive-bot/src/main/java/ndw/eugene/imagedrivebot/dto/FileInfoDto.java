package ndw.eugene.imagedrivebot.dto;

public record FileInfoDto
        (Long userId,
         String name,
         String description,
         String resource) {
}