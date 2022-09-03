package ndw.eugene.imagedrivebot.dto;

import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;

public record FormattedUpdate(
        long chatId,
        long userId,
        String messageText,
        String command,
        String parameter,
        String mediaGroupId,
        Message message,
        Document document
) {

    public boolean hasCommand() {
        return command != null;
    }

    public boolean hasParameter() {
        return parameter != null;
    }

    public boolean hasDocument() {
        return document != null;
    }

    public boolean messageTextIsNotEmpty() {
        return messageText != null && !messageText.isBlank();
    }

    public boolean hasMediaGroup() {
        return mediaGroupId != null;
    }

}
