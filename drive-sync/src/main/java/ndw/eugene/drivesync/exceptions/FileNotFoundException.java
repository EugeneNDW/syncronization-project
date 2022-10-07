package ndw.eugene.drivesync.exceptions;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String description) {
        super("cant find file with description: " + description);
    }
}
