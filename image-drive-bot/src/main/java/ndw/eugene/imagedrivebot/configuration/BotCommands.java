package ndw.eugene.imagedrivebot.configuration;

public enum BotCommands {

    UPLOAD_COMMAND("/upload"),

    END_CONVERSATION_COMMAND("/endconv"),

    START_COMMAND("/start"),

    RENAME_FOLDER_COMMAND("/rename_f");

    private final String command;

    BotCommands(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
