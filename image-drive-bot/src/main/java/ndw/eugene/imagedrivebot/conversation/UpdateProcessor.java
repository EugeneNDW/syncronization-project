package ndw.eugene.imagedrivebot.conversation;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateProcessor {
    void process(Update update, DriveSyncBot bot);
}