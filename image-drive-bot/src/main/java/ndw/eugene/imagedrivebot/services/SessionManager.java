package ndw.eugene.imagedrivebot.services;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import ndw.eugene.imagedrivebot.conversations.IConversation;
import ndw.eugene.imagedrivebot.conversations.uploadPhoto.PhotoUploadConversation;
import ndw.eugene.imagedrivebot.exceptions.SessionExpiredException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class SessionManager {

    private static final long DEFAULT_TIMEOUT = 3;

    private final Table<Long, Long, Session> usersSessions = HashBasedTable.create();

    public Session getSessionForUserInChat(long userId, long chatId) {
        Session session = usersSessions.get(userId, chatId);

        if (session == null) {
            return null;
        } else if (session.isEnded()) {
            removeSession(userId, chatId);
            return null;
        } else if (session.isExpired()) {
            throw new SessionExpiredException();
        }

        session.refreshTimeout();
        return session;
    }

    public Session startSession(long userId, long chatId, IConversation conversation) {
        Session session = new Session(conversation);
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

    public static class Session {
        private Instant startTime;
        private final long timeout;

        private final IConversation conversation;

        public Session(IConversation conversation) {
            this.conversation = conversation;

            this.startTime = Instant.now();
            this.timeout = DEFAULT_TIMEOUT;
        }

        public IConversation getConversation() {
            return conversation;
        }

        public boolean isExpired() {
            return startTime.plus(timeout, ChronoUnit.MINUTES).isBefore(Instant.now());
        }

        public boolean isEnded() {
            return conversation.isEnded();
        }

        public void refreshTimeout() {
            this.startTime = Instant.now();
        }

        private void close() {
            conversation.clearConversation();
        }
    }
}