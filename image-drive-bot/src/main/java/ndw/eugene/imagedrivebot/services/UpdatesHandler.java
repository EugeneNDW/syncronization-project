package ndw.eugene.imagedrivebot.services;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.configuration.BotCommand;
import ndw.eugene.imagedrivebot.configuration.BotMessage;
import ndw.eugene.imagedrivebot.dto.FormattedUpdate;
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

    public void handleUpdate(FormattedUpdate formattedUpdate, DriveSyncBot bot) {
        if (sessionManager.sessionExists(formattedUpdate.userId(), formattedUpdate.chatId())) {
            processSession(formattedUpdate, bot);
        } else {
            processCommand(formattedUpdate, bot);
        }
    }

    public void processCommand(FormattedUpdate update, DriveSyncBot bot) {
        if (Objects.equals(update.command(), BotCommand.START.getCommand())) {
            bot.sendMessageToChat(BotMessage.HELLO.getMessage(), update.chatId());
        } else if (Objects.equals(update.command(), BotCommand.UPLOAD.getCommand())) {
            conversationService.startUploadFileConversation(update.userId(), update.chatId());
            processSession(update, bot);
        } else if (Objects.equals(update.command(), BotCommand.RENAME_FOLDER.getCommand())) {
            fileService.renameChatFolder(update.chatId(), update.parameter());
            bot.sendMessageToChat(BotMessage.RENAME_FOLDER_SUCCESS.getMessage(), update.chatId());
        }
    }

    public void processSession(FormattedUpdate update, DriveSyncBot bot) {
        long chatId = update.chatId();
        long userId = update.userId();

        var session = sessionManager.getSessionForUserInChat(userId, chatId);
        if (session != null && !session.isExpired()) {
            if (Objects.equals(update.command(), BotCommand.END_CONVERSATION.getCommand())) {
                sessionManager.removeSession(userId, chatId);
                bot.sendMessageToChat(BotMessage.SESSION_WAS_CANCELED.getMessage(), chatId);
            } else {
                session.process(update, bot);
            }
        } else {
            sessionManager.removeSession(userId, chatId);
            bot.sendMessageToChat(BotMessage.SESSION_EXPIRED.getMessage(), chatId);
        }
    }
}
