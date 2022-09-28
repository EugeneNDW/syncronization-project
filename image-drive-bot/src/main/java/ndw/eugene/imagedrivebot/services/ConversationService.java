package ndw.eugene.imagedrivebot.services;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.conversations.IConversation;
import ndw.eugene.imagedrivebot.conversations.saveHistoryConversation.SaveHistoryConversation;
import ndw.eugene.imagedrivebot.conversations.saveHistoryConversation.SaveHistoryConversationProcessor;
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

    @Autowired
    private final SaveHistoryConversationProcessor saveHistoryConversationProcessor;

    public ConversationService(SessionManager sessionManager, PhotoUploadConversationProcessor uploadConversationProcessor, SaveHistoryConversationProcessor saveHistoryConversationProcessor) {
        this.sessionManager = sessionManager;
        this.uploadConversationProcessor = uploadConversationProcessor;
        this.saveHistoryConversationProcessor = saveHistoryConversationProcessor;
    }

    @Override
    public void startUploadFileConversation(Long userId, Long chatId) {
        var conversation = new PhotoUploadConversation();
        startConversation(userId, chatId, conversation);
    }

    @Override
    public void startSaveHistoryConversation(Long userId, Long chatId) {
        var conversation = new SaveHistoryConversation();
        startConversation(userId, chatId, conversation);
    }

    @Override
    public void processConversation(FormattedUpdate update, DriveSyncBot bot, IConversation conversation) {
        if (conversation instanceof PhotoUploadConversation photoUploadConversation) {
            uploadConversationProcessor.process(update, bot, photoUploadConversation);
        } else if (conversation instanceof SaveHistoryConversation saveHistoryConversation) {
            saveHistoryConversationProcessor.process(update, bot, saveHistoryConversation);
        }
    }

    private void startConversation(Long userId, Long chatId, IConversation conversation) {
        var session = sessionManager.getSessionForUserInChat(userId, chatId);
        if (session != null) {
            throw new SessionAlreadyExistsException();
        }
        sessionManager.startSession(userId, chatId, conversation);
    }
}
