package ndw.eugene.imagedrivebot;

import ndw.eugene.imagedrivebot.components.UpdateMapper;
import ndw.eugene.imagedrivebot.components.UpdatesHandler;
import ndw.eugene.imagedrivebot.dto.FileDownloadResult;
import ndw.eugene.imagedrivebot.components.BotExceptionsHandler;
import ndw.eugene.imagedrivebot.exceptions.NotAuthorizedException;
import ndw.eugene.imagedrivebot.services.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.Serializable;
import java.util.Set;

import static ndw.eugene.imagedrivebot.configurations.BotConfiguration.*;

public class DriveSyncBot extends TelegramLongPollingBot {

    private final String botName;

    private final String botToken;

    private final BotExceptionsHandler exceptionsHandler;

    private final UpdateMapper updateMapper;

    private final UpdatesHandler updatesHandler;

    private final IValidationService validationService;

    private final Set<Long> admins = Set.of(41809406L, 136094717L, 115364294L, 95263058L);

    public DriveSyncBot(
            String botName,
            String botToken,
            BotExceptionsHandler exceptionsHandler,
            UpdateMapper updateMapper,
            UpdatesHandler updatesHandler,
            IValidationService validationService) {
        this.exceptionsHandler = exceptionsHandler;
        this.botToken = botToken;
        this.botName = botName;
        this.updateMapper = updateMapper;
        this.updatesHandler = updatesHandler;
        this.validationService = validationService;
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
            if (!validationService.checkUpdateHasMessage(update) || !validationService.checkUpdateFromUser(update)) {
                return;
            }

            var formattedUpdate = updateMapper.formatUpdate(update);
            System.out.println(formattedUpdate.messageText());
            if (!admins.contains(formattedUpdate.userId())) {
                throw new NotAuthorizedException();
            }

            updatesHandler.handleUpdate(formattedUpdate, this);
        } catch (Exception e) {
            exceptionsHandler.handle(this, e, update);
        }
    }

    public FileDownloadResult downloadFile(Document document) {
        if (document.getFileSize() > MAX_FILE_SIZE_IN_BYTES) {
            return new FileDownloadResult(document.getFileName(), null, false);
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

    public void sendFileToChat(File file, Long chatId) {
        if (isPhoto(file)) {
            sendPhotoToChat(file, chatId);
        } else {
            sendDocumentToChat(file, chatId);
        }
    }

    public void sendPhotoToChat(File file, Long chatId) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId + "");
        photo.setPhoto(new InputFile(file));

        sendPhoto(photo);
    }

    public void sendMessageToChat(String message, Long chatId) {
        SendMessage m = new SendMessage();
        m.setChatId(chatId + "");
        m.setText(message);

        sendMessage(m);
    }

    public void sendDocumentToChat (File file, Long chatId) {
        SendDocument doc = new SendDocument();
        doc.setDocument(new InputFile(file));
        doc.setChatId(chatId + "");

        sendDocument(doc);
    }

    public void sendPhoto(SendPhoto photo) {
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e); //todo переделать обработку ошибки
        }
    }

    private void sendDocument(SendDocument doc) {
        try {
            execute(doc);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e); //todo переделать обработку ошибки
        }
    }

    public void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e); //todo переделать обработку ошибки
        }
    }

    private boolean isPhoto(File file) {
        return file.getName().endsWith(".jpg") || file.getName().endsWith(".png");
    }
}