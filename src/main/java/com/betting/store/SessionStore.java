package com.betting.store;

import com.betting.model.Session;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class SessionStore {
    private final Map<Integer, Session> customerToSession;
    private final Map<String, Session> sessionToCustomer;
    private final Map<Integer, ReentrantLock> customerLocks;

    public SessionStore() {
        this.customerToSession = new ConcurrentHashMap<>();
        this.sessionToCustomer = new ConcurrentHashMap<>();
        this.customerLocks = new ConcurrentHashMap<>();
    }

    public Optional<Session> getSessionByCustomerId(int customerId) {
        Session session = customerToSession.get(customerId);
        return Optional.ofNullable(session).filter(s -> !s.isExpired());
    }

    public Optional<Session> getSessionByKey(String sessionKey) {
        Session session = sessionToCustomer.get(sessionKey);
        return Optional.ofNullable(session).filter(s -> !s.isExpired());
    }

    public Session createOrUpdateSession(int customerId, String sessionKey, long durationSeconds) {
        ReentrantLock lock = customerLocks.computeIfAbsent(customerId, k -> new ReentrantLock());
        lock.lock();
        try {
            return getSessionByCustomerId(customerId)
                    .map(session -> updateSession(session, durationSeconds))
                    .orElseGet(() -> createNewSession(customerId, sessionKey, durationSeconds));
        } finally {
            lock.unlock();
        }
    }

    private Session updateSession(Session session, long durationSeconds) {
        Session updatedSession = session.extend(durationSeconds);
        customerToSession.put(session.getCustomerId(), updatedSession);
        sessionToCustomer.put(session.getSessionKey(), updatedSession);
        return updatedSession;
    }

    private Session createNewSession(int customerId, String sessionKey, long durationSeconds) {
        Session newSession = new Session(customerId, sessionKey,
                java.time.Instant.now().plusSeconds(durationSeconds));

        customerToSession.put(customerId, newSession);
        sessionToCustomer.put(sessionKey, newSession);

        return newSession;
    }

    public void cleanupExpiredSessions() {
        var now = java.time.Instant.now();

        customerToSession.entrySet().removeIf(entry -> {
            Session session = entry.getValue();
            boolean expired = session.isExpired(now);
            if (expired) {
                sessionToCustomer.remove(session.getSessionKey());
                customerLocks.remove(entry.getKey());
            }
            return expired;
        });
    }
}
