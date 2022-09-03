package ndw.eugene.imagedrivebot;

import ndw.eugene.imagedrivebot.configuration.BotCommands;
import ndw.eugene.imagedrivebot.dto.FormattedUpdate;
import ndw.eugene.imagedrivebot.exceptions.FileTooBigException;
import ndw.eugene.imagedrivebot.exceptions.NotAuthorizedException;
import ndw.eugene.imagedrivebot.services.ConversationService;
import ndw.eugene.imagedrivebot.services.IFileService;
import ndw.eugene.imagedrivebot.services.UpdateMapper;
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

    private final String botName;

    private final String botToken;

    private final SessionManager sessionManager;

    private final BotExceptionsHandler exceptionsHandler;

    private final UpdateMapper updateMapper;

    private final ConversationService conversationService;

    private final IFileService fileService;

    private final Set<Long> admins = Set.of(41809406L, 136094717L, 115364294L, 95263058L);

    public DriveSyncBot(
            String botName,
            String botToken,
            SessionManager sessionManager,
            BotExceptionsHandler exceptionsHandler,
            UpdateMapper updateMapper,
            ConversationService conversationService,
            IFileService fileService) {
        this.sessionManager = sessionManager;
        this.exceptionsHandler = exceptionsHandler;
        this.botToken = botToken;
        this.botName = botName;
        this.conversationService = conversationService;
        this.updateMapper = updateMapper;
        this.fileService = fileService;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (!checkUpdateHasMessage(update) || !checkUpdateFromUser(update)) {
                return;
            }

            var formattedUpdate = updateMapper.formatUpdate(update);
            System.out.println(formattedUpdate.messageText());
            if (!admins.contains(formattedUpdate.userId())) {
                throw new NotAuthorizedException(UNAUTHORIZED_MESSAGE);
            }

            if (sessionManager.sessionExists(formattedUpdate.userId(), formattedUpdate.chatId())) {
                processSession(formattedUpdate);
            } else {
                processCommand(formattedUpdate);
            }
        } catch (Exception e) {
            exceptionsHandler.handle(this, e, update);
        }
    }

    public File downloadFile(Document document) throws TelegramApiException {
        if (document.getFileSize() > MAX_FILE_SIZE_IN_BYTES) {
            throw new FileTooBigException("Can't upload file: " + document.getFileName() + " larger then 20mb");
        }

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

    private boolean checkUpdateHasMessage(Update update) {
        return update.getMessage() != null;
    }

    private boolean checkUpdateFromUser(Update update) {
        return update.getMessage().getFrom() != null;
    }

    private void processCommand(FormattedUpdate update) {
        if (Objects.equals(update.command(), BotCommands.START_COMMAND.getCommand())) {
            sendMessageToChat(HELLO_MESSAGE, update.chatId());
        } else if (Objects.equals(update.command(), BotCommands.UPLOAD_COMMAND.getCommand())) {
            conversationService.startUploadFileConversation(update.userId(), update.chatId());
            processSession(update);
        } else if (Objects.equals(update.command(), BotCommands.RENAME_FOLDER_COMMAND.getCommand())) {
            fileService.renameChatFolder(update.chatId(), update.parameter());
            sendMessageToChat(RENAME_FOLDER_SUCCESS_MESSAGE, update.chatId());
        }
    }

    private void processSession(FormattedUpdate update) {
        long chatId = update.chatId();
        long userId = update.userId();

        var session = sessionManager.getSessionForUserInChat(userId, chatId);
        if (session != null && !session.isExpired()) {
            if (Objects.equals(update.command(), BotCommands.END_CONVERSATION_COMMAND.getCommand())) {
                sessionManager.removeSession(userId, chatId);
                sendMessageToChat(SESSION_WAS_CANCELED_MESSAGE, chatId);
            } else {
                session.process(update, this);
            }
        } else {
            sessionManager.removeSession(userId, chatId);
            sendMessageToChat(SESSION_EXPIRED_MESSAGE, chatId);
        }
    }
}