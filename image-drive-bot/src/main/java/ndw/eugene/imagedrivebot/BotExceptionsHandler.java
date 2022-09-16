package ndw.eugene.imagedrivebot;

import ndw.eugene.imagedrivebot.exceptions.*;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static ndw.eugene.imagedrivebot.configuration.BotConfiguration.GENERIC_EXCEPTION_MESSAGE;
import static ndw.eugene.imagedrivebot.configuration.BotConfiguration.SERVER_ERROR_MESSAGE;

@Service
public class BotExceptionsHandler {

    private final SessionManager sessionManager;

    public BotExceptionsHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void handle(DriveSyncBot bot, Exception e, Update update) {
        Message message = update.getMessage();
        if (message != null) {
            boolean isCustomException = e instanceof CustomException;
            if (!isCustomException  || ((CustomException) e).isTerminateSession()) {
                sessionManager.removeSession(message.getFrom().getId(), message.getChatId());
            }
        }
        System.out.println(e.getMessage());
        if (e instanceof DocumentNotFoundException) {
            bot.sendMessageToChat(e.getMessage(), update.getMessage().getChatId());
        } else if (e instanceof NotAuthorizedException) {
            bot.sendMessageToChat(e.getMessage(), update.getMessage().getChatId());
        } else if (e instanceof DriveSyncException) {
            System.out.println(e.getMessage());
            bot.sendMessageToChat(SERVER_ERROR_MESSAGE, update.getMessage().getChatId());
        } else {
            bot.sendMessageToChat(GENERIC_EXCEPTION_MESSAGE, update.getMessage().getChatId());
        }
    }
}

