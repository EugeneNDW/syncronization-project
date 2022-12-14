package ndw.eugene.imagedrivebot.conversations;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.conversations.uploadPhoto.PhotoUploadConversation;
import ndw.eugene.imagedrivebot.dto.FormattedUpdate;

public interface UpdateProcessor<T extends IConversation> {
    void process(FormattedUpdate update, DriveSyncBot bot, T conversation);
}