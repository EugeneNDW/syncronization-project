package ndw.eugene.imagedrivebot.exceptions;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.configurations.BotMessage;
import ndw.eugene.imagedrivebot.services.SessionManager;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

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

        if (e instanceof CustomException) {
            bot.sendMessageToChat(e.getMessage(), update.getMessage().getChatId());
        } else {
            bot.sendMessageToChat(BotMessage.GENERIC_EXCEPTION.getMessage(), update.getMessage().getChatId());
        }
    }
}

