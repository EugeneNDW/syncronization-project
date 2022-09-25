package ndw.eugene.imagedrivebot.services;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import ndw.eugene.imagedrivebot.DriveSyncBot;
import ndw.eugene.imagedrivebot.conversation.uploadPhoto.PhotoUploadConversationProcessor;
import ndw.eugene.imagedrivebot.dto.FormattedUpdate;
import ndw.eugene.imagedrivebot.exceptions.SessionExpiredException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class SessionManager {
    private final Table<Long, Long, Session> usersSessions = HashBasedTable.create();

    //todo храним сессии по userId, chatId
    //todo отдаём сессию если она есть и активная
    //todo перед отдачей сессии обновляем таймер на ней
    //todo если время действия сессии истекло - выбрасываем ошибку
    //todo начинаем / удаляем сессию

    public Session getSessionForUserInChat(long userId, long chatId) {
        Session session = usersSessions.get(userId, chatId);

        if (session == null) {
            return null;
        } else if (session.isExpired()) {
            throw new SessionExpiredException();
        }

        return session;
    }

    public Session startSession(long userId, long chatId, PhotoUploadConversationProcessor conversationProcessor) {
        Session session = new Session(conversationProcessor);
        usersSessions.put(userId, chatId, session);

        return usersSessions.get(userId, chatId);
    }

    public void removeSession(long userId, long chatId) {
        var session = usersSessions.get(userId, chatId);
        if (session != null) {
            session.close();
        }
        usersSessions.remove(userId, chatId);
    }

    //todo хранит диалог
    //todo имеет таймер для таймаута
    //todo умеет отвечать закончилось ли время
    //todo умеет обновлять время таймера
    //todo умеет очищать свою информацию

    public static class Session {
        private static final long defaultTimeout = 3;
        private final Instant startTime;
        private final long timeout;

        private final PhotoUploadConversationProcessor conversationProcessor; //todo переделать на дженерик

        public Session(PhotoUploadConversationProcessor conversationProcessor) {
            this.conversationProcessor = conversationProcessor;

            this.startTime = Instant.now();
            this.timeout = defaultTimeout;
        }

        public boolean isExpired() {
            return startTime.plus(timeout, ChronoUnit.MINUTES).isBefore(Instant.now());
        }

        public PhotoUploadConversationProcessor getConversation() {
            return conversationProcessor;
        }

        public void process(FormattedUpdate update, DriveSyncBot bot) {
            conversationProcessor.process(update, bot);
        }

        private void close() {
            conversationProcessor.clearConversation();
        }
    }
}