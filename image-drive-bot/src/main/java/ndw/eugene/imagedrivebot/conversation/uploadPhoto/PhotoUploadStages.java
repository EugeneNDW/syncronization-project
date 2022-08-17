package ndw.eugene.imagedrivebot.conversation.uploadPhoto;

public enum PhotoUploadStages {
    CONVERSATION_STARTED,
    DESCRIPTION_PROVIDED,
    PHOTOS,
    ENDED;

    public static PhotoUploadStages getNextStage(PhotoUploadStages currentStage) {
        return switch (currentStage) {
            case CONVERSATION_STARTED -> DESCRIPTION_PROVIDED;
            case DESCRIPTION_PROVIDED -> PHOTOS;
            case PHOTOS, ENDED -> ENDED;
        };
    }
}
