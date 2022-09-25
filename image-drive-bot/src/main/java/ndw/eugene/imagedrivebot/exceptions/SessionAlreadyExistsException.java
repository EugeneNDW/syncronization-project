package ndw.eugene.imagedrivebot.exceptions;

import ndw.eugene.imagedrivebot.configurations.BotMessage;

public class SessionAlreadyExistsException extends CustomException{

    public SessionAlreadyExistsException() {
        super(BotMessage.SESSION_ALREADY_EXISTS_EXCEPTION.getMessage(), false);
    }
}
