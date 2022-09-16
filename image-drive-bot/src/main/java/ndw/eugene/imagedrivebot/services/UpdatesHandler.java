package ndw.eugene.imagedrivebot.services;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.configuration.BotCommand;
import ndw.eugene.imagedrivebot.configuration.BotMessage;
import ndw.eugene.imagedrivebot.dto.FormattedUpdate;
import ndw.eugene.imagedrivebot.exceptions.SessionAlreadyExistsException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UpdatesHandler {

    private final SessionManager sessionManager;
    private final ConversationService conversationService;
    private final IFileService fileService;

    public UpdatesHandler(SessionManager sessionManager, ConversationService conversationService, IFileService fileService) {
        this.sessionManager = sessionManager;
        this.conversationService = conversationService;
        this.fileService = fileService;
    }

    public void handleUpdate(FormattedUpdate update, DriveSyncBot bot) {
        var session = sessionManager.getSessionForUserInChat(update.userId(), update.chatId());
        if (Objects.equals(update.command(), BotCommand.START.getCommand())) {
            handleStart(update, bot);
        } else if (Objects.equals(update.command(), BotCommand.UPLOAD.getCommand())) {
            handleUpload(update, bot);
        } else if (Objects.equals(update.command(), BotCommand.RENAME_FOLDER.getCommand())) {
            handleRenameFolder(update, bot);
        } else if (Objects.equals(update.command(), BotCommand.END_CONVERSATION.getCommand())) {
            handleEndConversation(update, bot);
        } else if (session != null) {
            session.process(update, bot);
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
            session.process(update, bot);
        }
    }

    private void handleRenameFolder(FormattedUpdate update, DriveSyncBot bot) {
        fileService.renameChatFolder(update.chatId(), update.parameter());
        bot.sendMessageToChat(BotMessage.RENAME_FOLDER_SUCCESS.getMessage(), update.chatId());
    }
}
