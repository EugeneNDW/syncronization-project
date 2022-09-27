package ndw.eugene.imagedrivebot.services;

import ndw.eugene.imagedrivebot.dto.FormattedUpdate;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface IValidationService {
    boolean checkUpdateHasMessage(Update update);

    boolean checkUpdateFromUser(Update update);

    void checkUpdateHasDocument(FormattedUpdate update);
}
