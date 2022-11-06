package ndw.eugene.imagedrivebot.exceptions;

import ndw.eugene.imagedrivebot.configurations.BotMessage;

public class FolderNotFoundException extends CustomException{
    public FolderNotFoundException() {
        super(BotMessage.FOLDER_NOT_FOUND_EXCEPTION.getMessage(), false);
    }
}
