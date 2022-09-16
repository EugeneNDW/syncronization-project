package ndw.eugene.imagedrivebot.configuration;

public enum BotCommand {
    UPLOAD("/upload"),
    END_CONVERSATION("/endconv"),
    START("/start"),
    RENAME_FOLDER("/rename_f");

    private final String command;

    BotCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
