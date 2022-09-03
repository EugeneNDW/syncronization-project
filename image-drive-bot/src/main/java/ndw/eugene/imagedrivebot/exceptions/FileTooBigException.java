package ndw.eugene.imagedrivebot.exceptions;

public class FileTooBigException extends RuntimeException {
    public FileTooBigException(String message) {
        super(message);
    }
}
