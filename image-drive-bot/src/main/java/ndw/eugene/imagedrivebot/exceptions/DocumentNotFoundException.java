package ndw.eugene.imagedrivebot.exceptions;

import ndw.eugene.imagedrivebot.configurations.BotMessage;

public class DocumentNotFoundException extends CustomException {
    public DocumentNotFoundException() {
        super(BotMessage.DOCUMENT_NOT_FOUND_EXCEPTION.getMessage(), false);
    }
}
