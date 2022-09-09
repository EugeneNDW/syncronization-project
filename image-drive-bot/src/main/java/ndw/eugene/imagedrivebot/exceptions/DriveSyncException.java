package ndw.eugene.imagedrivebot.exceptions;

public class DriveSyncException extends CustomException {
    public DriveSyncException(String message) {
        super(message, true);
    }
}
