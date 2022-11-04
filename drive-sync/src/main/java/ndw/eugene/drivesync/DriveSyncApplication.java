package ndw.eugene.drivesync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
@EnableJpaAuditing
public class DriveSyncApplication {
    public static void main(String[] args) {
        SpringApplication.run(DriveSyncApplication.class, args);
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
    }
}