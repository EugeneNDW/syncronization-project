package ndw.eugene.imagedrivebot.conversations.saveHistoryConversation;

import ndw.eugene.imagedrivebot.conversations.IConversation;

public class SaveHistoryConversation implements IConversation {

    private SaveHistoryStages currentStage = SaveHistoryStages.CONVERSATION_STARTED;

    @Override
    public void clearConversation() {
    }

    public SaveHistoryStages getCurrentStage() {
        return currentStage;
    }

    @Override
    public boolean isEnded() {
        return currentStage == SaveHistoryStages.ENDED;
    }

    public void nextStage() {
        currentStage = SaveHistoryStages.getNextStage(currentStage);
    }
}
