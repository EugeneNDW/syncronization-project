package ndw.eugene.imagedrivebot.conversations.saveHistoryConversation;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.configurations.BotMessage;
import ndw.eugene.imagedrivebot.conversations.UpdateProcessor;
import ndw.eugene.imagedrivebot.dto.FormattedUpdate;
import ndw.eugene.imagedrivebot.exceptions.DocumentNotFoundException;
import ndw.eugene.imagedrivebot.services.IFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class SaveHistoryConversationProcessor {

    private final String HISTORY_TAG = "history";

    @Autowired
    private final IFileService fileService;

    public SaveHistoryConversationProcessor(IFileService fileService) {
        this.fileService = fileService;
    }

    public void process(FormattedUpdate update, DriveSyncBot bot, SaveHistoryConversation conversation) {
        var processor = getStageProcessor(conversation.getCurrentStage());
        processor.process(update, bot, conversation);
        nextStage(conversation);
    }

    private UpdateProcessor<SaveHistoryConversation> getStageProcessor(SaveHistoryStages currentStage) {
        return switch (currentStage) {
            case CONVERSATION_STARTED -> startProcessor;
            case DOCUMENT_UPLOADED -> documentProcessor;
            case ENDED -> endedProcessor;
        };
    }

    public final UpdateProcessor<SaveHistoryConversation> startProcessor = (update, bot, conversation) ->
            bot.sendMessageToChat(BotMessage.UPLOAD_START.getMessage(), update.chatId());

    public final UpdateProcessor<SaveHistoryConversation> documentProcessor = this::sendPhotos;

    public final UpdateProcessor<SaveHistoryConversation> endedProcessor = (update, bot, conversation) -> {
        throw new IllegalArgumentException(BotMessage.CANT_REACH_EXCEPTION.getMessage());
    };

    private void nextStage(SaveHistoryConversation conversation) {
        conversation.nextStage();
    }

    private void sendPhotos(FormattedUpdate update, DriveSyncBot bot, SaveHistoryConversation conversation) {
        if (!update.hasDocument()) {
            throw new DocumentNotFoundException();
        }
        var results = fileService.synchronizeFiles(bot,
                update.chatId(),
                update.userId(),
                HISTORY_TAG,
                Collections.singletonList(update.document()));

        var messageText = results
                .stream()
                .map( r -> r.fileName() + " " + (r.successStatus() ? "сохранён в истории" : "не был вписан в историю, попробуйте ещё раз"))
                .collect(Collectors.joining(""));

        bot.sendMessageToChat(messageText, update.chatId());
    }
}
