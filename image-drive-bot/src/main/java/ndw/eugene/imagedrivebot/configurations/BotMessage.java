package ndw.eugene.imagedrivebot.configurations;

public enum BotMessage {
    HELLO("привет, я синхробот синхронизирую файлы"),
    UPLOAD_START("начинаем загрузку фотографий, введите описание для загружаемых фото " +
            "или команду /skip чтобы оставить описание пустым"),
    DESCRIPTION_SAVED("описание сохранено. теперь загрузите фотографии без сжатия, размером не более 20мб каждая"),
    RENAME_FOLDER_SUCCESS("папка была переименована"),
    DOCUMENT_NOT_FOUND_EXCEPTION("не удалось найти документ в сообщении. возможно вы не прикрепили фотографии, " +
            "либо прикрепили фотографии со сжатием, попробуйте ещё раз"),
    CANT_REACH_EXCEPTION("диалог закончился, сессия должна быть удалена, как мы здесь оказались"),
    GENERIC_EXCEPTION("что-то случилось, мы всё записали и обязательно разберемся. попробуйте позже"),
    UNAUTHORIZED_EXCEPTION("знакомы?"),
    SESSION_ALREADY_EXISTS_EXCEPTION("у вас уже начат диалог в этом чате, доведите его до конца, " +
            "либо завершите при помощи команды /endconv"),
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