package ndw.eugene.imagedrivebot.services;

import ndw.eugene.imagedrivebot.dto.FormattedUpdate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class ValidationService implements IValidationService {
    @Override
    public boolean checkUpdateHasMessage(Update update) {
        return update.getMessage() != null;
    }

    @Override
    public boolean checkUpdateFromUser(Update update) {
        Message message = update.getMessage();
        if (message == null) {
            return false;
        }

        return message.getFrom() != null;
    }

    @Override
    public boolean checkUpdateIsTextMessage(FormattedUpdate update) {
        return update.messageText() != null;
    }

    @Override
    public boolean checkUpdateHasDocument(FormattedUpdate update) {
        return update.hasDocument();
    }
}
