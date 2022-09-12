package ndw.eugene.imagedrivebot.exceptions;

import ndw.eugene.imagedrivebot.configuration.BotConfiguration;

public class DocumentNotFoundException extends CustomException {
    public DocumentNotFoundException() {
        super(BotConfiguration.DOCUMENT_NOT_FOUND_EXCEPTION_MESSAGE, false);
    }
}
