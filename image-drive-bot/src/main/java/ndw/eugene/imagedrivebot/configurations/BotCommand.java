package ndw.eugene.imagedrivebot.configurations;

public enum BotCommand {
    UPLOAD("/upload"),
    SAVE_HISTORY("/save_history"),
    RANDOM_HISTORY("/random_history"),
    END_CONVERSATION("/endconv"),
    START("/start"),
    RENAME_FOLDER("/rename_f"),
    CREATE_FOLDER("/create_f"),
    SKIP_DESCRIPTION("/skip");

    private final String command;

    BotCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
