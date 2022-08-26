package ndw.eugene.imagedrivebot;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import ndw.eugene.imagedrivebot.conversation.uploadPhoto.PhotoUploadConversationProcessor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class SessionManager {
//базовый флоу работы с сессиями, пока что предполагаем что конкурентного доступа нету, потом перепишется на конкурентность
    private final Table<Long, Long, Session> usersSessions = HashBasedTable.create();

    public Session getSessionForUserInChat(long userId, long chatId) {
        Session session = usersSessions.get(userId, chatId);
        if (session != null && session.isEnded()) {
            usersSessions.remove(userId, chatId);
            return null;
        }

        return session;
    }

    public Session startSession(long userId, long chatId, PhotoUploadConversationProcessor conversationProcessor) {
        Session session = new Session(userId, chatId, conversationProcessor);
        usersSessions.put(userId, chatId, session);

        return usersSessions.get(userId, chatId);
    }

    public void removeSession(long userId, long chatId) {
        usersSessions.remove(userId, chatId);
    }

    public boolean sessionExists(long userId, long chatId) {
        return getSessionForUserInChat(userId, chatId) != null;
    }

    public static class Session {
        private static final long defaultTimeout = 3;
        private final long chatId;
        private final long userId;
        private final Instant startTime;
        private final long timeout;

        private final PhotoUploadConversationProcessor conversationProcessor;

        public Session(long userId, long chatId, PhotoUploadConversationProcessor conversationProcessor) {
            this.chatId = chatId;
            this.userId = userId;
            this.conversationProcessor = conversationProcessor;

            this.startTime = Instant.now();
            this.timeout = defaultTimeout;
        }

        public boolean isActive() {
            return !isExpired() && !isEnded();
        }

        public boolean isExpired() {
            return startTime.plus(timeout, ChronoUnit.MINUTES).isBefore(Instant.now());
        }

        public boolean isEnded() {
            return getConversation().isEnded();
        }

        public PhotoUploadConversationProcessor getConversation() {
            return conversationProcessor;
        }

        public void process(FormattedUpdate update, DriveSyncBot bot) {
            conversationProcessor.process(update, bot);
        }
    }
}