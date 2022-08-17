package ndw.eugene.imagedrivebot.conversation.uploadPhoto;

import ndw.eugene.imagedrivebot.conversation.ConversationState;


public class PhotoUploadConversationState implements ConversationState {

    private PhotoUploadStages currentStage = PhotoUploadStages.CONVERSATION_STARTED;

    @Override
    public PhotoUploadStages getCurrentStage() {
        return currentStage;
    }

    @Override
    public boolean isEnded() {
        return currentStage == PhotoUploadStages.ENDED;
    }

    @Override
    public void nextStage() {
        currentStage = PhotoUploadStages.getNextStage(currentStage);
    }
}
