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
        return "syncfilesbot";
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
                var message = new SendMessage();
                message.setChatId(update.getMessage().getChatId() + "");
                message.setText("знакомы?");
                sendMessage(message);
            }
        } catch (DocumentNotFoundException e) {
            var message = new SendMessage();
            message.setChatId(update.getMessage().getChatId() + "");
            message.setText(e.getMessage());
            sendMessage(message);
        } catch (Exception e) {
            var message = new SendMessage();
            message.setChatId(update.getMessage().getChatId() + "");
            message.setText("что-то случилось, мы всё записали и обязательно разберемся. Попробуйте ещё раз или ещё раз, но позже");
            sendMessage(message);
        }
    }

    public void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void processCommand(Update update) {
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        String messageText = message.getText();
        if (Objects.equals(messageText, "/upload")) {
            var session = conversationService.startUploadFileConversation(userId, chatId);
            processSession(update, session);
        } else {
            SendMessage m = new SendMessage();
            m.setChatId(chatId + "");
            m.setText("произошла обработка команды");

            sendMessage(m);
        }
    }

    private void processSession(Update update, SessionManager.Session session) {
        if (!session.isExpired()) {
            session.getConversation().process(update, this); //encapsulate conversation and session inside structure
        } else {
            sessionManager.removeSession(session);

            SendMessage m = new SendMessage();
            m.setChatId(update.getMessage().getChatId() + "");
            m.setText("сессия протухла");
            sendMessage(m);
        }
    }

    public File downloadFile(Document document) throws TelegramApiException {
        var outputFile = new java.io.File(System.getProperty("java.io.tmpdir") + "/" + document.getFileName());
        GetFile getFile = new GetFile();
        getFile.setFileId(document.getFileId());
        String filePath = execute(getFile).getFilePath();
        return downloadFile(filePath, outputFile);
    }
}