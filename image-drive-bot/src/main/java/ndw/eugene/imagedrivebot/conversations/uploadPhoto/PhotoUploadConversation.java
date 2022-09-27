package ndw.eugene.imagedrivebot.conversations.uploadPhoto;

import org.telegram.telegrambots.meta.api.objects.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class PhotoUploadConversation {
    private PhotoUploadStages currentStage = PhotoUploadStages.CONVERSATION_STARTED;

    private String mediaGroupId = null;

    private final List<Document> documents = new ArrayList<>();

    private String description;

    private boolean isTaskDone = false;

    private ScheduledFuture<?> job = null;

    public boolean isTaskDone() {
        return isTaskDone;
    }

    public void setTaskDone(boolean taskDone) {
        isTaskDone = taskDone;
    }

    public String getMediaGroupId() {
        return mediaGroupId;
    }

    public void setMediaGroupId(String mediaGroupId) {
        this.mediaGroupId = mediaGroupId;
    }

    public ScheduledFuture<?> getJob() {
        return job;
    }

    public void setJob(ScheduledFuture<?> job) {
        this.job = job;
    }

    public void clearConversation() {
        if (job != null) {
            job.cancel(false);
        }
        documents.clear();
    }

    public PhotoUploadStages getCurrentStage() {
        return currentStage;
    }

    public boolean isEnded() {
        return currentStage == PhotoUploadStages.ENDED;
    }

    public void nextStage() {
        currentStage = PhotoUploadStages.getNextStage(currentStage);
    }

    public void addFile(Document file) {
        documents.add(file);
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
