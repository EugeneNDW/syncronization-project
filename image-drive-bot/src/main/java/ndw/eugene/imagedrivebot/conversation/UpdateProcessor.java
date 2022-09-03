package ndw.eugene.imagedrivebot.conversation;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.dto.FormattedUpdate;

public interface UpdateProcessor {
    void process(FormattedUpdate update, DriveSyncBot bot);
}