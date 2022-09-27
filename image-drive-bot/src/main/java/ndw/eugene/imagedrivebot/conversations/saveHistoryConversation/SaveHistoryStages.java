package ndw.eugene.imagedrivebot.conversations.saveHistoryConversation;

public enum SaveHistoryStages {
    CONVERSATION_STARTED,
    DOCUMENT_UPLOADED,
    ENDED;

    public static SaveHistoryStages getNextStage(SaveHistoryStages currentStage) {
        return switch (currentStage) {
            case CONVERSATION_STARTED -> DOCUMENT_UPLOADED;
            case DOCUMENT_UPLOADED, ENDED -> ENDED;
        };
    }
}
