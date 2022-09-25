package ndw.eugene.imagedrivebot.conversations;

import ndw.eugene.imagedrivebot.conversations.uploadPhoto.PhotoUploadStages;

public interface ConversationState {
    PhotoUploadStages getCurrentStage();
    boolean isEnded();
    void nextStage();
}
