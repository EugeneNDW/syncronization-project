package ndw.eugene.imagedrivebot;

import ndw.eugene.imagedrivebot.configuration.BotCommand;
import ndw.eugene.imagedrivebot.configuration.BotMessage;
import ndw.eugene.imagedrivebot.dto.FileDownloadResult;
import ndw.eugene.imagedrivebot.dto.FormattedUpdate;
import ndw.eugene.imagedrivebot.exceptions.NotAuthorizedException;
import ndw.eugene.imagedrivebot.services.*;
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
                throw new NotAuthorizedException();
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

    public FileDownloadResult downloadFile(Document document) {
        if (document.getFileSize() > MAX_FILE_SIZE_IN_BYTES) {
            return new FileDownloadResult(document.getFileName(),null, false);
        }
        try {
            var outputFile = new File(System.getProperty("java.io.tmpdir") + "/" + document.getFileName());

            GetFile getFile = new GetFile();
            getFile.setFileId(document.getFileId());
            String filePath = execute(getFile).getFilePath();

            File file = downloadFile(filePath, outputFile);
            return new FileDownloadResult(document.getFileName(), file, true);
        } catch (TelegramApiException e) {
            return new FileDownloadResult(document.getFileName(), null, false);
        }
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

    private void processCommand(FormattedUpdate update) {
        if (Objects.equals(update.command(), BotCommand.START.getCommand())) {
            sendMessageToChat(BotMessage.HELLO.getMessage(), update.chatId());
        } else if (Objects.equals(update.command(), BotCommand.UPLOAD.getCommand())) {
            conversationService.startUploadFileConversation(update.userId(), update.chatId());
            processSession(update);
        } else if (Objects.equals(update.command(), BotCommand.RENAME_FOLDER.getCommand())) {
            fileService.renameChatFolder(update.chatId(), update.parameter());
            sendMessageToChat(BotMessage.RENAME_FOLDER_SUCCESS.getMessage(), update.chatId());
        }
    }

    private void processSession(FormattedUpdate update) {
        long chatId = update.chatId();
        long userId = update.userId();

        var session = sessionManager.getSessionForUserInChat(userId, chatId);
        if (session != null && !session.isExpired()) {
            if (Objects.equals(update.command(), BotCommand.END_CONVERSATION.getCommand())) {
                sessionManager.removeSession(userId, chatId);
                sendMessageToChat(BotMessage.SESSION_WAS_CANCELED.getMessage(), chatId);
            } else {
                session.process(update, this);
            }
        } else {
            sessionManager.removeSession(userId, chatId);
            sendMessageToChat(BotMessage.SESSION_EXPIRED.getMessage(), chatId);
        }
    }

    //todo extract to validator
    private boolean checkUpdateHasMessage(Update update) {
        return update.getMessage() != null;
    }

    private boolean checkUpdateFromUser(Update update) {
        return update.getMessage().getFrom() != null;
    }

}