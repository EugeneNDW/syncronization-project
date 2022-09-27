package ndw.eugene.imagedrivebot.services;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.conversations.IConversation;
import ndw.eugene.imagedrivebot.conversations.uploadPhoto.PhotoUploadConversation;
import ndw.eugene.imagedrivebot.conversations.uploadPhoto.PhotoUploadConversationProcessor;
import ndw.eugene.imagedrivebot.dto.FormattedUpdate;
import ndw.eugene.imagedrivebot.exceptions.SessionAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConversationService implements IConversationService {

    @Autowired
    private final SessionManager sessionManager;

    @Autowired
    private final PhotoUploadConversationProcessor uploadConversationProcessor;

    public ConversationService(SessionManager sessionManager, PhotoUploadConversationProcessor uploadConversationProcessor) {
        this.sessionManager = sessionManager;
        this.uploadConversationProcessor = uploadConversationProcessor;
    }

    @Override
    public void startUploadFileConversation(Long userId, Long chatId) {
        var session = sessionManager.getSessionForUserInChat(userId, chatId);
        if (session != null) {
            throw new SessionAlreadyExistsException();
        }
        var conversation = new PhotoUploadConversation();
        sessionManager.startSession(userId, chatId, conversation);
    }

    @Override
    public void processConversation(FormattedUpdate update, DriveSyncBot bot, IConversation conversation) {
        if (conversation instanceof PhotoUploadConversation photoUploadConversation) {
            uploadConversationProcessor.process(update, bot, photoUploadConversation);
        }
    }
}
