package ndw.eugene.imagedrivebot.exceptions;

public class NotAuthorizedException extends CustomException{
    public NotAuthorizedException(String message) {
        super(message, true);
    }
}
