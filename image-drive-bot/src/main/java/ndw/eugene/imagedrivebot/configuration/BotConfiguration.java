package ndw.eugene.imagedrivebot.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import ndw.eugene.imagedrivebot.BotExceptionsHandler;
import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.SessionManager;
import ndw.eugene.imagedrivebot.services.ConversationService;
import ndw.eugene.imagedrivebot.services.IFileService;
import ndw.eugene.imagedrivebot.services.UpdateMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfiguration {

    public static final String SESSION_EXPIRED_MESSAGE = "сессия протухла, чтобы начать новую введите: /update";

    public static final String SESSION_WAS_CANCELED_MESSAGE = "сессия удалена. можно начать новую, " +
            "либо использовать команду.";

    public static final String HELLO_MESSAGE = "привет, я синхробот синхронизирую файлы";

    public static final String GENERIC_EXCEPTION_MESSAGE = "что-то случилось, мы всё записали и обязательно " +
            "разберемся. Попробуйте ещё раз или ещё раз, но позже";

    public static final String SERVER_ERROR_MESSAGE = "что-то сервер не справляется, " +
            "мы посмотрим и всё починим. попробуйте позже";

    public static final String UNAUTHORIZED_MESSAGE = "знакомы?";

    public static final String UPLOAD_START_MESSAGE = "начинаем загрузку фотографий, " +
            "введите описание для загружаемых фото";

    public static final String UPLOAD_DESCRIPTION_SAVED_MESSAGE = "описание сохранено. теперь загрузите " +
            "фотографии без сжатия, размером не более 20мб каждая";

    public static final String RENAME_FOLDER_SUCCESS_MESSAGE = "папка была переименована";

    public static final String DOCUMENT_NOT_FOUND_EXCEPTION_MESSAGE = "Не удалось найти документ в сообщении. " +
            "Возможно вы не прикрепили фотографии, " +
            "либо прикрепили фотографии со сжатием, попробуйте ещё раз";

    public static final String CANT_REACH_EXCEPTION_MESSAGE = "Диалог закончился, " +
            "сессия должна быть удалена, " +
            "выполнение не должно доходить до этого момента";

    public static final String RESOURCE_NAME = "TELEGRAM";

    private final String botToken;

    private final String botName;

    public BotConfiguration(
            @Value("${application.telegrambot.token}") String botToken,
            @Value("${application.telegrambot.name}") String botName
    ) {
        this.botToken = botToken;
        this.botName = botName;
    }

    @Bean
    public TelegramBotsApi getBotsApi(DriveSyncBot bot) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
            return telegramBotsApi;
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public DriveSyncBot getDriveSyncBot(SessionManager sessionManager,
                                        BotExceptionsHandler exceptionsHandler,
                                        ConversationService conversationService,
                                        UpdateMapper updateMapper,
                                        IFileService fileService) {
        return new DriveSyncBot(
                botName,
                botToken,
                sessionManager,
                exceptionsHandler,
                updateMapper,
                conversationService,
                fileService
        );
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public TaskScheduler getTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("scheduler-thread");
        scheduler.initialize();

        return scheduler;
    }
}