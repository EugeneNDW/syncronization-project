package ndw.eugene.imagedrivebot.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.SessionManager;
import ndw.eugene.imagedrivebot.services.ConversationService;
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

    private final String botToken;

    public static final String RESOURCE_NAME = "TELEGRAM";
    public BotConfiguration(@Value("${application.telegrambot.token}") String botToken) {
        this.botToken = botToken;
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
                                        ConversationService conversationService) {
        return new DriveSyncBot(sessionManager, botToken, conversationService);
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