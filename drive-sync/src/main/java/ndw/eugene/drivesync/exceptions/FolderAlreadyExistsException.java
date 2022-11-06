package ndw.eugene.drivesync.exceptions;

public class FolderAlreadyExistsException extends RuntimeException{
    public FolderAlreadyExistsException(long chatId) {
        super("chat with id:" + chatId + " already has folder");
    }
}
