package ndw.eugene.imagedrivebot.services;

import ndw.eugene.imagedrivebot.dto.FormattedUpdate;
import ndw.eugene.imagedrivebot.exceptions.DocumentNotFoundException;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class ValidationService implements IValidationService {
    @Override
    public boolean checkUpdateHasMessage(Update update) {
        return update.getMessage() != null;
    }

    @Override
    public boolean checkUpdateFromUser(Update update) {
        return update.getMessage().getFrom() != null;
    }

    @Override
    public void checkUpdateHasDocument(FormattedUpdate update) {
        if (!update.hasDocument()) {
            throw new DocumentNotFoundException();
        }
    }
}
