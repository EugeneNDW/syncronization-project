package ndw.eugene.imagedrivebot.conversation;

import ndw.eugene.imagedrivebot.conversation.uploadPhoto.PhotoUploadStages;

public interface ConversationState {
    PhotoUploadStages getCurrentStage();
    boolean isEnded();
    void nextStage();
}
