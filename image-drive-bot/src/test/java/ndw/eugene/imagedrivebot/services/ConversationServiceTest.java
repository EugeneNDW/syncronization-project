package ndw.eugene.imagedrivebot.services;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.components.SessionManager;
import ndw.eugene.imagedrivebot.conversations.saveHistoryConversation.SaveHistoryConversation;
import ndw.eugene.imagedrivebot.conversations.saveHistoryConversation.SaveHistoryConversationProcessor;
import ndw.eugene.imagedrivebot.conversations.uploadPhoto.PhotoUploadConversation;
import ndw.eugene.imagedrivebot.conversations.uploadPhoto.PhotoUploadConversationProcessor;
import ndw.eugene.imagedrivebot.dto.FormattedUpdate;
import ndw.eugene.imagedrivebot.exceptions.SessionAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class ConversationServiceTest {

    private ConversationService service;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private PhotoUploadConversationProcessor photoUploadConversationProcessor;

    @Mock
    private SaveHistoryConversationProcessor saveHistoryConversationProcessor;

    @Mock
    private DriveSyncBot bot;

    private FormattedUpdate update;

    private final long chatId = 2L;
    
    private final long userId = 1L;

    @BeforeEach
    public void init() {
        update = new FormattedUpdate(
                userId,
                chatId,
                "message",
                "command",
                "parameter",
                "mediaGroupId",
                null,
                null
        );
        service = new ConversationService(
                sessionManager,
                photoUploadConversationProcessor,
                saveHistoryConversationProcessor
        );
    }

    @Test
    public void start_upload_conversation() {
        service.startUploadFileConversation(userId, chatId);

        Mockito.verify(sessionManager).getSessionForUserInChat(userId, chatId);
        Mockito.verify(sessionManager).startSession(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.any(PhotoUploadConversation.class)
        );
    }

    @Test
    public void start_history_conversation() {
        service.startSaveHistoryConversation(userId, chatId);

        Mockito.verify(sessionManager).getSessionForUserInChat(userId, chatId);
        Mockito.verify(sessionManager).startSession(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.any(SaveHistoryConversation.class)
        );
    }

    @Test
    public void start_upload_conversation_session_already_exists() {
        doReturn(new SessionManager.Session(new PhotoUploadConversation()))
                .when(sessionManager)
                .getSessionForUserInChat(userId, chatId);

        assertThrows(SessionAlreadyExistsException.class, () -> service.startUploadFileConversation(userId, chatId));
    }

    @Test
    public void start_history_conversation_session_already_exists() {
        doReturn(new SessionManager.Session(new SaveHistoryConversation()))
                .when(sessionManager)
                .getSessionForUserInChat(userId, chatId);

        assertThrows(SessionAlreadyExistsException.class, () -> service.startUploadFileConversation(userId, chatId));
    }

    @Test
    public void process_upload_conversation() {
        var conversation = new PhotoUploadConversation();
        service.processConversation(update, bot, conversation);

        Mockito.verify(photoUploadConversationProcessor).process(update, bot, conversation);
    }

    @Test
    public void process_history_conversation() {
        var conversation = new SaveHistoryConversation();
        service.processConversation(update, bot, conversation);

        Mockito.verify(saveHistoryConversationProcessor).process(update, bot, conversation);
    }
}
