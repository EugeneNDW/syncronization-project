package ndw.eugene.drivesync.exceptions;

public class DriveException extends RuntimeException {
    public DriveException(Throwable cause) {
        super("Can't connect to google drive, try later", cause);
    }
}
