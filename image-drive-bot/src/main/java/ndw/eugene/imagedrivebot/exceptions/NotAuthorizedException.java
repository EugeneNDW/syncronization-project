package ndw.eugene.imagedrivebot.exceptions;

import ndw.eugene.imagedrivebot.configuration.BotMessage;

public class NotAuthorizedException extends CustomException{
    public NotAuthorizedException() {
        super(BotMessage.UNAUTHORIZED_EXCEPTION.getMessage(), true);
    }
}
