package ndw.eugene.imagedrivebot.exceptions;

public class DocumentNotFoundException extends CustomException {
    public DocumentNotFoundException(String message) {
        super(message, false);
    }
}
