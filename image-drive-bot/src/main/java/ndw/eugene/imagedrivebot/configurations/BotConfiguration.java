package ndw.eugene.imagedrivebot.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import ndw.eugene.imagedrivebot.exceptions.BotExceptionsHandler;
import ndw.eugene.imagedrivebot.services.*;
import ndw.eugene.imagedrivebot.DriveSyncBot;
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

    public static final int MAX_FILE_SIZE_IN_BYTES = 20_971_520;

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
    public DriveSyncBot getDriveSyncBot(BotExceptionsHandler exceptionsHandler,
                                        UpdateMapper updateMapper,
                                        UpdatesHandler updatesHandler) {

        return new DriveSyncBot(botName, botToken, exceptionsHandler, updateMapper, updatesHandler);
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