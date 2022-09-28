package ndw.eugene.imagedrivebot.services;

import ndw.eugene.imagedrivebot.dto.FormattedUpdate;
import ndw.eugene.imagedrivebot.exceptions.DocumentNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationServiceTest {

    private ValidationService service;

    @BeforeEach
    public void setUp() {
        service = new ValidationService();
    }

    @Test
    public void check_update_from_user_success() {
        var update = new Update();
        var message = new Message();
        message.setFrom(new User());
        update.setMessage(message);

        assertTrue(service.checkUpdateFromUser(update));
    }

    @Test
    public void check_update_from_user_failure() {
        var update = new Update();
        update.setMessage(new Message());

        assertFalse(service.checkUpdateFromUser(update));
    }

    @Test
    public void check_update_has_message_failure() {
        var update = new Update();

        assertFalse(service.checkUpdateHasMessage(update));
    }

    @Test
    public void check_update_has_message_success() {
        var update = new Update();
        update.setMessage(new Message());

        assertTrue(service.checkUpdateHasMessage(update));
    }

    @Test
    public void check_update_has_document_failure() {
        var update = new FormattedUpdate(
                1,
                1,
                "message",
                "command",
                "parameter",
                "mediaGroupId",
                null,
                null
        );

        assertThrows(DocumentNotFoundException.class, () -> service.checkUpdateHasDocument(update));
    }

    @Test
    public void check_update_has_document_success() {
        var update = new FormattedUpdate(
                1,
                1,
                "message",
                "command",
                "parameter",
                "mediaGroupId",
                null,
                new Document()
        );

        assertDoesNotThrow(() -> service.checkUpdateHasDocument(update));
    }
}
