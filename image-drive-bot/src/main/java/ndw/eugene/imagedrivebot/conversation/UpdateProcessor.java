package ndw.eugene.imagedrivebot.conversation;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.FormattedUpdate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateProcessor {
    void process(FormattedUpdate update, DriveSyncBot bot);
}