package ndw.eugene.drivesync.exceptions;

public class FolderNotFoundException extends RuntimeException {
    public FolderNotFoundException(long chatId) {
        super("cant find folder in chat with id: " + chatId);
    }
}
