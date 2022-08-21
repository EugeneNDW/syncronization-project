package ndw.eugene.imagedrivebot;

import ndw.eugene.imagedrivebot.exceptions.DocumentNotFoundException;
import ndw.eugene.imagedrivebot.services.ConversationService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.Objects;

import static ndw.eugene.imagedrivebot.configuration.BotConfiguration.*;

public class DriveSyncBot extends TelegramLongPollingBot {

    private final SessionManager sessionManager;
    private final String botToken;

    private final ConversationService conversationService;

    private final Long adminId = 95263058L;

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
            Message incMessage = update.getMessage();
            Long chatId = incMessage.getChatId();
            Long userId = incMessage.getFrom().getId();

            if (userId.equals(adminId)) {
                System.out.println(incMessage.getText());
                var session = sessionManager.getSessionForUserInChat(userId, chatId);
                if (session != null) {
                    processSession(update, session);
                } else {
                    processCommand(update);
                }
            } else {
                sendMessageToChat(UNAUTHORIZED_MESSAGE, update.getMessage().getChatId());
            }
        } catch (DocumentNotFoundException e) {
            sendMessageToChat(e.getMessage(), update.getMessage().getChatId());
        } catch (Exception e) {
            sendMessageToChat(GENERIC_EXCEPTION_MESSAGE, update.getMessage().getChatId());
        }
    }

    private void processCommand(Update update) {
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        String messageText = message.getText();
        if (Objects.equals(messageText, "/start")) {
            sendMessageToChat(HELLO_MESSAGE, chatId);
        } else if (Objects.equals(messageText, "/upload")) {
            var session = conversationService.startUploadFileConversation(userId, chatId);
            processSession(update, session);
        }
    }

    private void processSession(Update update, SessionManager.Session session) {
        if (!session.isExpired()) {
            session.getConversation().process(update, this); //encapsulate conversation and session inside structure
        } else {
            sessionManager.removeSession(session);


            sendMessageToChat(SESSION_EXPIRED_MESSAGE, update.getMessage().getChatId());
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
            throw new RuntimeException(e);
        }
    }
}