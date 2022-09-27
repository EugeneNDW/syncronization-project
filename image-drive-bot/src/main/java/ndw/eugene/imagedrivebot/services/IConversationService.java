package ndw.eugene.imagedrivebot.services;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.conversations.uploadPhoto.PhotoUploadConversation;
import ndw.eugene.imagedrivebot.dto.FormattedUpdate;

public interface IConversationService {
    void startUploadFileConversation(Long userId, Long chatId);

    void processConversation(FormattedUpdate update, DriveSyncBot bot, PhotoUploadConversation conversation);
}
