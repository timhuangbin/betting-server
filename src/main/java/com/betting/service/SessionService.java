package com.betting.service;

import com.betting.model.Session;
import com.betting.store.SessionStore;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class SessionService {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SESSION_KEY_LENGTH = 8;
    private static final long SESSION_DURATION_SECONDS = 10 * 60;

    private final SessionStore sessionStore;

    public SessionService(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    public String getOrCreateSession(int customerId) {
        Optional<Session> existingSession = sessionStore.getSessionByCustomerId(customerId);
        if (existingSession.isPresent()) {
            Session updated = sessionStore.createOrUpdateSession(
                    customerId, existingSession.get().getSessionKey(), SESSION_DURATION_SECONDS);
            return updated.getSessionKey();
        }

        String sessionKey = generateUniqueSessionKey();
        Session newSession = sessionStore.createOrUpdateSession(
                customerId, sessionKey, SESSION_DURATION_SECONDS);

        return newSession.getSessionKey();
    }

    public boolean isValidSession(String sessionKey) {
        return sessionStore.getSessionByKey(sessionKey).isPresent();
    }

    public Optional<Integer> getCustomerId(String sessionKey) {
        return sessionStore.getSessionByKey(sessionKey).map(Session::getCustomerId);
    }

    public void cleanupExpiredSessions() {
        sessionStore.cleanupExpiredSessions();
    }

    private String generateUniqueSessionKey() {
        String sessionKey;
        int attempts = 0;
        do {
            if (attempts++ > 100) {
                throw new IllegalStateException("Failed to generate unique session key");
            }
            sessionKey = generateRandomString();
        } while (sessionStore.getSessionByKey(sessionKey).isPresent());

        return sessionKey;
    }

    private String generateRandomString() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        char[] chars = new char[SESSION_KEY_LENGTH];
        for (int i = 0; i < SESSION_KEY_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            chars[i] = CHARACTERS.charAt(index);
        }
        return new String(chars);
    }
}
