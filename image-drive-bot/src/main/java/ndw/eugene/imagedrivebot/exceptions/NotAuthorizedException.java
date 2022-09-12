package ndw.eugene.imagedrivebot.exceptions;

import ndw.eugene.imagedrivebot.configuration.BotConfiguration;

public class NotAuthorizedException extends CustomException{
    public NotAuthorizedException() {
        super(BotConfiguration.UNAUTHORIZED_MESSAGE, true);
    }
}
