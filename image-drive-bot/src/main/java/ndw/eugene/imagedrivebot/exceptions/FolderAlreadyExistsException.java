package ndw.eugene.imagedrivebot.exceptions;

import ndw.eugene.imagedrivebot.configurations.BotMessage;

public class FolderAlreadyExistsException extends CustomException{
    public FolderAlreadyExistsException() {
        super(BotMessage.FOLDER_ALREADY_EXISTS_EXCEPTION.getMessage(), false);
    }
}
