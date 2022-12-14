package ndw.eugene.imagedrivebot.components;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.configurations.BotCommand;
import ndw.eugene.imagedrivebot.configurations.BotMessage;
import ndw.eugene.imagedrivebot.dto.FormattedUpdate;
import ndw.eugene.imagedrivebot.services.IConversationService;
import ndw.eugene.imagedrivebot.services.IFileService;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UpdatesHandler {

    private final SessionManager sessionManager;
    private final IConversationService conversationService;
    private final IFileService fileService;

    public UpdatesHandler(SessionManager sessionManager, IConversationService conversationService, IFileService fileService) {
        this.sessionManager = sessionManager;
        this.conversationService = conversationService;
        this.fileService = fileService;
    }

    public void handleUpdate(FormattedUpdate update, DriveSyncBot bot) {
        if (Objects.equals(update.command(), BotCommand.START.getCommand())) {
            handleStart(update, bot);
        } else if (Objects.equals(update.command(), BotCommand.UPLOAD.getCommand())) {
            handleUpload(update, bot);
        } else if (Objects.equals(update.command(), BotCommand.RENAME_FOLDER.getCommand())) {
            handleRenameFolder(update, bot);
        } else if (Objects.equals(update.command(), BotCommand.END_CONVERSATION.getCommand())) {
            handleEndConversation(update, bot);
        } else if (Objects.equals(update.command(), BotCommand.SAVE_HISTORY.getCommand())) {
            handleSaveHistory(update, bot);
        } else if (Objects.equals(update.command(), BotCommand.RANDOM_HISTORY.getCommand())) {
            handleRandomHistory(update, bot);
        } else if (Objects.equals(update.command(), BotCommand.CREATE_FOLDER.getCommand())) {
            handleCreateFolder(update, bot);
        } else {
            handleConversation(update, bot);
        }
    }

    private void handleConversation(FormattedUpdate update, DriveSyncBot bot) {
        var session = sessionManager.getSessionForUserInChat(update.userId(), update.chatId());
        if (session != null) {
            var conversation = session.getConversation();
            conversationService.processConversation(update, bot, conversation);
        }
    }

    private void handleEndConversation(FormattedUpdate update, DriveSyncBot bot) {
        sessionManager.removeSession(update.userId(), update.chatId());
        bot.sendMessageToChat(BotMessage.SESSION_WAS_CANCELED.getMessage(), update.chatId());
    }

    private void handleStart(FormattedUpdate update, DriveSyncBot bot) {
        bot.sendMessageToChat(BotMessage.HELLO.getMessage(), update.chatId());
    }

    private void handleUpload(FormattedUpdate update, DriveSyncBot bot) {
        conversationService.startUploadFileConversation(update.userId(), update.chatId());
        var session = sessionManager.getSessionForUserInChat(update.userId(), update.chatId());
        if (session != null) {
            conversationService.processConversation(update, bot, session.getConversation());
        }
    }

    private void handleSaveHistory(FormattedUpdate update, DriveSyncBot bot) {
        conversationService.startSaveHistoryConversation(update.userId(), update.chatId());
        var session = sessionManager.getSessionForUserInChat(update.userId(), update.chatId());
        if (session != null) {
            conversationService.processConversation(update, bot, session.getConversation());
        }
    }

    private void handleCreateFolder(FormattedUpdate update, DriveSyncBot bot) {
        fileService.createChatFolder(update.chatId(), update.hasParameter() ? update.parameter() : update.chatId() + "");
        bot.sendMessageToChat(BotMessage.CREATE_FOLDER_SUCCESS.getMessage(), update.chatId());
    }

    private void handleRenameFolder(FormattedUpdate update, DriveSyncBot bot) {
        fileService.renameChatFolder(update.chatId(), update.hasParameter() ? update.parameter() : update.chatId() + "");
        bot.sendMessageToChat(BotMessage.RENAME_FOLDER_SUCCESS.getMessage(), update.chatId());
    }

    private void handleRandomHistory(FormattedUpdate update, DriveSyncBot bot) {
        var file = fileService.searchFile(update.chatId(), "history");
        bot.sendFileToChat(file, update.chatId());
    }
}
