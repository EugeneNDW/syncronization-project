package ndw.eugene.imagedrivebot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class ImageDriveBotApplication {
	public static void main(String[] args) {
		SpringApplication.run(ImageDriveBotApplication.class, args);
		System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
	}
}
