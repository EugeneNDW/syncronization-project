package ndw.eugene.imagedrivebot.services;

import ndw.eugene.imagedrivebot.configurations.BotCommand;
import ndw.eugene.imagedrivebot.dto.FormattedUpdate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

@Service
public class UpdateMapper {

    private final String botName;

    public UpdateMapper(@Value("${application.telegrambot.name}") String botName) {
        this.botName = botName;
    }

    //we don't check nullability of message and getFrom() because we consider such updates as invalid and won't process them.
    public FormattedUpdate formatUpdate(Update update) {
        var message = update.getMessage();

        var chatId = message.getChatId();
        var userId = message.getFrom().getId();
        var messageText = message.getText();
        var command = "";
        var parameter = "";
        var mediaGroupId = message.getMediaGroupId();
        var document = message.getDocument();

        if (stringIsNotEmpty(messageText)) {
            var splittedMessage = messageText.split(" ", 2);
            var updateCommand = splittedMessage[0].replace("@" + botName, "");
            if (validateCommand(updateCommand)) {
                command = updateCommand;
                if (splittedMessage.length == 2) {
                    parameter = splittedMessage[1];
                }
            }
        }

        return new FormattedUpdate(chatId, userId, messageText, command, parameter, mediaGroupId, message, document);
    }

    private boolean validateCommand(String command) {
        var commands = BotCommand.values();
        for (BotCommand botCommand : commands) {
            if (Objects.equals(botCommand.getCommand(), command)) {
                return true;
            }
        }
        return false;
    }

    private boolean stringIsNotEmpty(String str) {
        return str != null && !str.isBlank();
    }
}
