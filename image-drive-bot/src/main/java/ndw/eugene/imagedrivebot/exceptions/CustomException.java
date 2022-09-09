package ndw.eugene.imagedrivebot.exceptions;

public class CustomException extends RuntimeException {
    private final boolean terminateSession;

    public CustomException(String message, boolean session) {
        super(message);
        this.terminateSession = session;
    }

    public boolean isTerminateSession() {
        return terminateSession;
    }
}
