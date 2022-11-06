package ndw.eugene.imagedrivebot.configurations;

public enum BotMessage {
    HELLO("привет, я синхробот синхронизирую файлы"),
    UPLOAD_START("начинаем загрузку фотографий, введите описание для загружаемых фото " +
            "или команду /skip чтобы оставить описание пустым"),
    HISTORY_START("начинаем загружать документ для истории. отправьте файл который хотите сохранить," +
            " он будет помечен специальным тегом"),
    DESCRIPTION_SAVED("описание сохранено. теперь загрузите фотографии без сжатия, размером не более 20мб каждая"),
    RENAME_FOLDER_SUCCESS("папка была переименована"),
    CREATE_FOLDER_SUCCESS("папка была создана"),
    DOCUMENT_NOT_FOUND_EXCEPTION("не удалось найти документ в сообщении. возможно вы не прикрепили фотографии, " +
            "либо прикрепили фотографии со сжатием, попробуйте ещё раз"),
    CANT_REACH_EXCEPTION("диалог закончился, сессия должна быть удалена, как мы здесь оказались"),
    GENERIC_EXCEPTION("что-то случилось, мы всё записали и обязательно разберемся. попробуйте позже"),
    UNAUTHORIZED_EXCEPTION("знакомы?"),
    SESSION_ALREADY_EXISTS_EXCEPTION("у вас уже начат диалог в этом чате, доведите его до конца, " +
            "либо завершите при помощи команды /endconv"),
    FOLDER_NOT_FOUND_EXCEPTION("не удалось найти папку для текущего чата, вы можете создать её при помощи команды /create_f *имя папки*"),
    FOLDER_ALREADY_EXISTS_EXCEPTION("в этом чате уже создана папка. на данный момент поддерживается только одна папка для чата"),
    SESSION_EXPIRED("сессия протухла, чтобы начать новую введите: /upload"),
    SESSION_WAS_CANCELED("сессия удалена. можно начать новую, либо использовать команду."),
    ;
    private final String message;

    BotMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}