package com.betting.model;

import java.time.Instant;

public class Session {
    private final int customerId;
    private final String sessionKey;
    private final Instant expiryTime;

    public Session(int customerId, String sessionKey, Instant expiryTime) {
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
        return Instant.now().isAfter(expiryTime);
    }

    public boolean isExpired(Instant currentTime) {
        return currentTime.isAfter(expiryTime);
    }

    public Session extend(long durationSeconds) {
        return new Session(customerId, sessionKey, expiryTime.plusSeconds(durationSeconds));
    }
}
