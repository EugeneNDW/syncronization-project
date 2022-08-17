package ndw.eugene.drivesync.exceptions;

public class TikaException extends RuntimeException{
    public TikaException(Throwable cause) {
        super("Can't decide file's mime type", cause);
    }
}
