package ndw.eugene.imagedrivebot.conversations;

import ndw.eugene.imagedrivebot.conversations.uploadPhoto.PhotoUploadStages;

public interface IConversation {
    void clearConversation();

    boolean isEnded();
}
