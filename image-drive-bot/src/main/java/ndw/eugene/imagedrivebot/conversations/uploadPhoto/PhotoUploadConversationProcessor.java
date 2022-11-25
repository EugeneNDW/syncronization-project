package ndw.eugene.imagedrivebot.conversations.uploadPhoto;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.configurations.BotCommand;
import ndw.eugene.imagedrivebot.configurations.BotMessage;
import ndw.eugene.imagedrivebot.conversations.UpdateProcessor;
import ndw.eugene.imagedrivebot.dto.FormattedUpdate;
import ndw.eugene.imagedrivebot.exceptions.DocumentNotFoundException;
import ndw.eugene.imagedrivebot.services.IFileService;
import ndw.eugene.imagedrivebot.services.IValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import static ndw.eugene.imagedrivebot.configurations.BotMessage.FAILURE_SYMBOL;
import static ndw.eugene.imagedrivebot.configurations.BotMessage.SUCCESS_SYMBOL;

@Component
public class PhotoUploadConversationProcessor {
    private final int WAIT_FOR_UPDATES_LIMIT_IN_SEC = 30;
    @Autowired
    private final IFileService fileService;
    @Autowired
    private final TaskScheduler scheduler;
    @Autowired
    private IValidationService validationService;

    public final UpdateProcessor<PhotoUploadConversation> startProcessor = (update, bot, conversation) ->
            bot.sendMessageToChat(BotMessage.UPLOAD_START.getMessage(), update.chatId());
    public final UpdateProcessor<PhotoUploadConversation> descriptionProcessor = (update, bot, conversation) -> {
        var description = "";
        boolean skipDescription = Objects.equals(update.command(), BotCommand.SKIP_DESCRIPTION.getCommand());
        if (!skipDescription) {
            description = update.messageTextIsNotEmpty() ? update.messageText() : "";
        }

        conversation.setDescription(description);
        bot.sendMessageToChat(BotMessage.DESCRIPTION_SAVED.getMessage(), update.chatId());
    };

    public final UpdateProcessor<PhotoUploadConversation> photosProcessor = (update, bot, conversation) -> {
        if (validationService.checkUpdateIsTextMessage(update)) {
            return;
        }
        if (!validationService.checkUpdateHasDocument(update)) {
            throw new DocumentNotFoundException();
        }

        var updateMediaGroup = update.mediaGroupId();
        var document = update.document();
        String mediaGroupId = conversation.getMediaGroupId();
        boolean updateHasGroup = update.hasMediaGroup();
        boolean groupIsSet = mediaGroupId != null;

        boolean groupIsSetAndUpdateWithoutGroup = !updateHasGroup && groupIsSet;
        boolean updateFromAnotherGroup = updateHasGroup && groupIsSet && !mediaGroupId.equals(updateMediaGroup);
        if (groupIsSetAndUpdateWithoutGroup || updateFromAnotherGroup) {
            return;
        }

        conversation.addFile(document);

        if (!updateHasGroup) {
            sendPhotos(update, bot, conversation);
        } else if (!groupIsSet) {
            conversation.setMediaGroupId(updateMediaGroup);
            conversation.setJob(schedulePhotoUpload(update, bot, conversation));
        } else if (mediaGroupId.equals(updateMediaGroup)) {
            if (conversation.getJob() != null) {
                conversation.getJob().cancel(false);
            }
            conversation.setJob(schedulePhotoUpload(update, bot, conversation));
        }
    };

    public final UpdateProcessor<PhotoUploadConversation> endedProcessor = (update, bot, conversation) -> {
        throw new IllegalArgumentException(BotMessage.CANT_REACH_EXCEPTION.getMessage());
    };

    public PhotoUploadConversationProcessor(IFileService fileService, IValidationService validationService, TaskScheduler scheduler) {
        this.fileService = fileService;
        this.validationService = validationService;
        this.scheduler = scheduler;
    }

    public void process(FormattedUpdate update, DriveSyncBot bot, PhotoUploadConversation conversation) {
        var processor = getStageProcessor(conversation.getCurrentStage());
        processor.process(update, bot, conversation);
        nextStage(conversation);
    }

    private void nextStage(PhotoUploadConversation conversation) {
        var currentStage = conversation.getCurrentStage();
        var isPhotoStage = currentStage == PhotoUploadStages.PHOTOS;

        if (!isPhotoStage || conversation.isTaskDone()) {
            conversation.nextStage();
        }
    }

    private UpdateProcessor<PhotoUploadConversation> getStageProcessor(PhotoUploadStages currentStage) {
        return switch (currentStage) {
            case CONVERSATION_STARTED -> startProcessor;
            case DESCRIPTION_PROVIDED -> descriptionProcessor;
            case PHOTOS -> photosProcessor;
            case ENDED -> endedProcessor;
        };
    }

    private ScheduledFuture<?> schedulePhotoUpload(FormattedUpdate update, DriveSyncBot bot, PhotoUploadConversation conversation) {
        return scheduler.schedule(
                () -> sendPhotos(update, bot, conversation),
                Instant.now().plus(WAIT_FOR_UPDATES_LIMIT_IN_SEC, ChronoUnit.SECONDS)
        );
    }

    private void sendPhotos(FormattedUpdate update, DriveSyncBot bot, PhotoUploadConversation conversation) {
        conversation.setTaskDone(true);
        var results = fileService.synchronizeFiles(
                bot,
                update.chatId(),
                update.userId(),
                conversation.getDescription(),
                false,
                conversation.getDocuments());

        var result = results
                .stream()
                .map(r -> (r.successStatus() ? SUCCESS_SYMBOL.getMessage() : FAILURE_SYMBOL.getMessage()) + " " + r.fileName())
                .collect(Collectors.joining("\n", "Загружено: \n", ""));

        bot.sendMessageToChat(result, update.chatId());
        nextStage(conversation);
    }
}
