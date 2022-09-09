package ndw.eugene.imagedrivebot.exceptions;

public class FileTooBigException extends CustomException {
    public FileTooBigException(String message) {
        super(message, false);
    }
}
