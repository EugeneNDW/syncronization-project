package ndw.eugene.imagedrivebot.exceptions;

import ndw.eugene.imagedrivebot.configurations.BotMessage;

public class SessionExpiredException extends CustomException {
    public SessionExpiredException() {
        super(BotMessage.SESSION_EXPIRED.getMessage(), true);
    }
}
