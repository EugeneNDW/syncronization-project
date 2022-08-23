package ndw.eugene.imagedrivebot.conversation.uploadPhoto;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.conversation.UpdateProcessor;
import ndw.eugene.imagedrivebot.dto.FileInfoDto;
import ndw.eugene.imagedrivebot.exceptions.DocumentNotFoundException;
import ndw.eugene.imagedrivebot.services.IFileService;
import org.springframework.scheduling.TaskScheduler;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;

import static ndw.eugene.imagedrivebot.configuration.BotConfiguration.*;

public class PhotoUploadConversationProcessor {
    private final int WAIT_FOR_UPDATES_LIMIT_IN_SEC = 30;
    private final PhotoUploadConversationState conversationState = new PhotoUploadConversationState();
    private String mediaGroupId = null;
    private final PhotoUploadData photosData = new PhotoUploadData();
    private final IFileService fileService;
    private final TaskScheduler scheduler;
    private boolean isTaskDone = false;
    private ScheduledFuture<?> job = null;

    public PhotoUploadConversationProcessor(IFileService fileService, TaskScheduler scheduler) {
        this.fileService = fileService;
        this.scheduler = scheduler;
    }

    public void process(Update update, DriveSyncBot bot) {
        UpdateProcessor processor = getStageProcessor(conversationState.getCurrentStage());
        processor.process(update, bot);
        nextStage();
    }

    private void nextStage() {
        var currentStage = conversationState.getCurrentStage();
        var isPhotoStage = currentStage == PhotoUploadStages.PHOTOS;

        if (!isPhotoStage || isTaskDone) {
            conversationState.nextStage();
        }
    }

    public boolean isEnded() {
        return conversationState.isEnded();
    }

    public UpdateProcessor getStageProcessor(PhotoUploadStages currentStage) {
        return switch (currentStage) {
            case CONVERSATION_STARTED -> startProcessor;
            case DESCRIPTION_PROVIDED -> descriptionProcessor;
            case PHOTOS -> photosProcessor;
            case ENDED -> endedProcessor;
        };
    }

    public final UpdateProcessor startProcessor = (update, bot) ->
            bot.sendMessageToChat(UPLOAD_START_MESSAGE, update.getMessage().getChatId());

    public final UpdateProcessor descriptionProcessor = (update, bot) -> {
        Message message = update.getMessage();
        if (message != null) {
            var description = message.getText() != null ? message.getText() : "";
            var userId = message.getFrom().getId();
            photosData.setDescription(description + " " + userId);

            bot.sendMessageToChat(UPLOAD_DESCRIPTION_SAVED_MESSAGE, message.getChatId());
        }
    };

    public final UpdateProcessor photosProcessor = (update, bot) -> {
        var message = update.getMessage();
        if (message != null) {
            var updateMediaGroup = message.getMediaGroupId();
            var document = message.getDocument();
            if (document == null) {
                throw new DocumentNotFoundException(DOCUMENT_NOT_FOUND_EXCEPTION_MESSAGE);
            }
            boolean documentFromAnotherGroup = mediaGroupId != null && !mediaGroupId.equals(updateMediaGroup);
            if (documentFromAnotherGroup) {
                return;
            }

            File file;
            try {
                file = bot.downloadFile(document);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e); //todo костыль, подумать когда скачивать файлы и что делать с ошибкой.
            }
            photosData.addFile(file);
            if (mediaGroupId == null) {
                if (updateMediaGroup != null) {
                    mediaGroupId = updateMediaGroup;
                    job = schedulePhotoUpload(update, bot);
                } else {
                    sendPhotos(update, bot);
                }
            } else if (mediaGroupId.equals(updateMediaGroup)) {
                if (job != null) {
                    job.cancel(false);
                }
                job = schedulePhotoUpload(update, bot);
            }
        }
    };

    public final UpdateProcessor endedProcessor = (update, bot) -> {
        throw new IllegalArgumentException(CANT_REACH_EXCEPTION_MESSAGE);
    };

    private ScheduledFuture<?> schedulePhotoUpload(Update update, DriveSyncBot bot) {
        return scheduler.schedule(() -> {
            sendPhotos(update, bot);
        }, Instant.now().plus(WAIT_FOR_UPDATES_LIMIT_IN_SEC, ChronoUnit.SECONDS));
    }

    private void sendPhotos(Update update, DriveSyncBot bot) {
        isTaskDone = true;
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        photosData.getUploadedFiles()
                .forEach(f ->
                        fileService.sendFileToDisk(f,
                                new FileInfoDto(
                                        chatId,
                                        userId,
                                        f.getName(),
                                        photosData.getDescription(),
                                        RESOURCE_NAME))
                );

        bot.sendMessageToChat("загружено:" + photosData.getUploadedFiles().size() + " фотографий", chatId);
        nextStage();
    }
}
