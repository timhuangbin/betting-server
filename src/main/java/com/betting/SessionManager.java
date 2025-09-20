package com.betting;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final long SESSION_DURATION = 10 * 60 * 1000; // 10分钟
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SESSION_KEY_LENGTH = 8;

    private final Map<Integer, SessionInfo> customerSessions;
    private final Map<String, SessionInfo> sessionCustomers;

    public SessionManager() {
        this.customerSessions = new ConcurrentHashMap<>();
        this.sessionCustomers = new ConcurrentHashMap<>();
    }

    public String getOrCreateSession(int customerId) {
        SessionInfo sessionInfo = customerSessions.get(customerId);

        if (sessionInfo != null && !sessionInfo.isExpired()) {
            // 更新过期时间
            sessionInfo.extend(SESSION_DURATION);
            return sessionInfo.getSessionKey();
        }

        // 创建新会话
        String sessionKey = generateSessionKey();
        long expiryTime = System.currentTimeMillis() + SESSION_DURATION;

        sessionInfo = new SessionInfo(customerId, sessionKey, expiryTime);

        customerSessions.put(customerId, sessionInfo);
        sessionCustomers.put(sessionKey, sessionInfo);

        return sessionKey;
    }

    public boolean isValidSession(String sessionKey) {
        SessionInfo sessionInfo = sessionCustomers.get(sessionKey);
        return sessionInfo != null && !sessionInfo.isExpired();
    }

    public Integer getCustomerId(String sessionKey) {
        SessionInfo sessionInfo = sessionCustomers.get(sessionKey);
        if (sessionInfo != null && !sessionInfo.isExpired()) {
            return sessionInfo.getCustomerId();
        }
        return null;
    }

    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();

        // 清理过期的会话
        customerSessions.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
        sessionCustomers.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private String generateSessionKey() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(SESSION_KEY_LENGTH);

        for (int i = 0; i < SESSION_KEY_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }

        return sb.toString();
    }

    private static class SessionInfo {
        private final int customerId;
        private final String sessionKey;
        private long expiryTime;

        public SessionInfo(int customerId, String sessionKey, long expiryTime) {
            this.customerId = customerId;
            this.sessionKey = sessionKey;
            this.expiryTime = expiryTime;
        }

        public int getCustomerId() {
            return customerId;
        }

        public String getSessionKey() {
            return sessionKey;
        }

        public boolean isExpired() {
            return isExpired(System.currentTimeMillis());
        }

        public boolean isExpired(long currentTime) {
            return currentTime > expiryTime;
        }

        public void extend(long duration) {
            this.expiryTime = System.currentTimeMillis() + duration;
        }
    }
}
