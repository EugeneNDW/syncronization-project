package ndw.eugene.imagedrivebot;

import ndw.eugene.imagedrivebot.exceptions.DocumentNotFoundException;
import ndw.eugene.imagedrivebot.services.ConversationService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.Objects;
import java.util.Set;

import static ndw.eugene.imagedrivebot.configuration.BotConfiguration.*;

public class DriveSyncBot extends TelegramLongPollingBot {

    private final SessionManager sessionManager;

    private final String botToken;

    private final ConversationService conversationService;

    private final Set<Long> admins = Set.of(41809406L, 136094717L, 115364294L, 95263058L);

    public DriveSyncBot(SessionManager sessionManager, String botToken, ConversationService conversationService) {
        this.sessionManager = sessionManager;
        this.botToken = botToken;
        this.conversationService = conversationService;
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (!checkUpdateFromUser(update) || !checkUpdateHasMessage(update)) {
                return;
            }
            var formattedUpdate = formatUpdate(update);
            System.out.println(formattedUpdate.getMessageText());
            Long userId = formattedUpdate.getUserId();
            Long chatId = formattedUpdate.getChatId();

            if (!admins.contains(userId)) {
                sendMessageToChat(UNAUTHORIZED_MESSAGE, chatId);
                return;
            }

            var session = sessionManager.getSessionForUserInChat(userId, chatId);
            if (session != null) {
                processSession(formattedUpdate, session);
            } else {
                processCommand(formattedUpdate);
            }
        } catch (DocumentNotFoundException e) {
            System.out.println(e.getMessage());
            sendMessageToChat(e.getMessage(), update.getMessage().getChatId());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            sendMessageToChat(GENERIC_EXCEPTION_MESSAGE, update.getMessage().getChatId());
        }
    }

    private boolean checkUpdateHasMessage(Update update) {
        return update.getMessage() != null;
    }

    private boolean checkUpdateFromUser(Update update) {
        return update.getMessage() != null;
    }

    private FormattedUpdate formatUpdate(Update update) {
        return new FormattedUpdate(update);
    }

    private void processCommand(FormattedUpdate update) {
        Long chatId = update.getChatId();
        Long userId = update.getUserId();

        String messageText = update.getMessageText();
        if (Objects.equals(messageText, START_COMMAND)) {
            sendMessageToChat(HELLO_MESSAGE, chatId);
        } else {
            if (Objects.equals(messageText, UPLOAD_COMMAND)) {
                var session = conversationService.startUploadFileConversation(userId, chatId);
                processSession(update, session);
            }
        }
    }

    private void processSession(FormattedUpdate update, SessionManager.Session session) {
        if (!session.isExpired()) {
            session.process(update, this);
        } else {
            sessionManager.removeSession(session);
            sendMessageToChat(SESSION_EXPIRED_MESSAGE, update.getChatId());
        }
    }

    public File downloadFile(Document document) throws TelegramApiException {
        var outputFile = new java.io.File(System.getProperty("java.io.tmpdir") + "/" + document.getFileName());
        GetFile getFile = new GetFile();
        getFile.setFileId(document.getFileId());
        String filePath = execute(getFile).getFilePath();
        return downloadFile(filePath, outputFile);
    }

    public void sendMessageToChat(String message, Long chatId) {
        SendMessage m = new SendMessage();
        m.setChatId(chatId + "");
        m.setText(message);

        sendMessage(m);
    }

    public void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e); //todo переделать обработку ошибки
        }
    }
}