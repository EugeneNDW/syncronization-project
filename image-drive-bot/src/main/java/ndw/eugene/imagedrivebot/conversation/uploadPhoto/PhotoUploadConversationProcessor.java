package ndw.eugene.imagedrivebot.conversation.uploadPhoto;

import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.dto.FormattedUpdate;
import ndw.eugene.imagedrivebot.conversation.UpdateProcessor;
import ndw.eugene.imagedrivebot.dto.FileInfoDto;
import ndw.eugene.imagedrivebot.exceptions.DocumentNotFoundException;
import ndw.eugene.imagedrivebot.services.IFileService;
import org.springframework.scheduling.TaskScheduler;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;

import static ndw.eugene.imagedrivebot.configuration.BotConfiguration.*;

public class PhotoUploadConversationProcessor {
    private final int WAIT_FOR_UPDATES_LIMIT_IN_SEC = 30;
    private final PhotoUploadConversationState conversationState = new PhotoUploadConversationState();
    private final PhotoUploadData photosData = new PhotoUploadData();
    private final IFileService fileService;
    private final TaskScheduler scheduler;
    private boolean isTaskDone = false;
    private String mediaGroupId = null;
    private ScheduledFuture<?> job = null;

    public PhotoUploadConversationProcessor(IFileService fileService, TaskScheduler scheduler) {
        this.fileService = fileService;
        this.scheduler = scheduler;
    }

    public void process(FormattedUpdate update, DriveSyncBot bot) {
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
            bot.sendMessageToChat(UPLOAD_START_MESSAGE, update.chatId());

    public final UpdateProcessor descriptionProcessor = (update, bot) -> {
        var description = update.messageTextIsNotEmpty() ? update.messageText() : "";
        photosData.setDescription(description + " " + update.userId());
        bot.sendMessageToChat(UPLOAD_DESCRIPTION_SAVED_MESSAGE, update.chatId());
    };


    //приходит апдейт с null, у нас есть группа                             -> пропускаем
    //приходит апдейт с группой, у нас есть группа и она не такая группа    -> пропускаем
    //приходит апдейт с null, у нас нету группы                             -> грузим одну фотку
    //приходит апдейт с группой, у нас есть группа и она такая же группа    -> добавляем в фотки, обновляем джобу
    //приходит апдейт с группой, у нас нет группы                           -> добавляем в фотки, устанавливаем группу, начинаем джобу

    public final UpdateProcessor photosProcessor = (update, bot) -> {
        //валидируем
        if (!update.hasDocument()) {
            throw new DocumentNotFoundException(DOCUMENT_NOT_FOUND_EXCEPTION_MESSAGE);
        }
        var updateMediaGroup = update.mediaGroupId();
        var document = update.document();
        boolean updateHasGroup = update.hasMediaGroup();
        boolean weHaveGroup = mediaGroupId != null;
        boolean weHaveGroupUpdateNot = !updateHasGroup && weHaveGroup;
        boolean updateFromAnotherGroup = updateHasGroup && weHaveGroup && !mediaGroupId.equals(updateMediaGroup);
        if (weHaveGroupUpdateNot || updateFromAnotherGroup) {
            return;
        }

        //делаем работу
        try {
            File file;
            file = bot.downloadFile(document);
            photosData.addFile(file);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e); //todo костыль, подумать когда скачивать файлы и что делать с ошибкой.
        }

        if (!updateHasGroup) {
            sendPhotos(update, bot);
        } else if (!weHaveGroup) {
            mediaGroupId = updateMediaGroup;
            job = schedulePhotoUpload(update, bot);
        } else if (mediaGroupId.equals(updateMediaGroup)) {
            if (job != null) {
                job.cancel(false);
            }
            job = schedulePhotoUpload(update, bot);
        }
    };

    public final UpdateProcessor endedProcessor = (update, bot) -> {
        throw new IllegalArgumentException(CANT_REACH_EXCEPTION_MESSAGE);
    };

    public void clearConversation() {
        if (job != null) {
            job.cancel(false);
        }
        photosData.getUploadedFiles().clear();
    }

    private ScheduledFuture<?> schedulePhotoUpload(FormattedUpdate update, DriveSyncBot bot) {
        return scheduler.schedule(
                () -> sendPhotos(update, bot),
                Instant.now().plus(WAIT_FOR_UPDATES_LIMIT_IN_SEC, ChronoUnit.SECONDS)
        );
    }

    private void sendPhotos(FormattedUpdate update, DriveSyncBot bot) {
        isTaskDone = true;
        long chatId = update.chatId();
        long userId = update.userId();
        photosData.getUploadedFiles()
                .forEach(f ->
                        fileService.sendFileToDisk(
                                chatId,
                                f,
                                new FileInfoDto(
                                        userId,
                                        f.getName(),
                                        photosData.getDescription(),
                                        RESOURCE_NAME
                                )
                        )
                );
        bot.sendMessageToChat("загружено: " + photosData.getUploadedFiles().size() + " фотографий", chatId);
        nextStage();
    }
}
