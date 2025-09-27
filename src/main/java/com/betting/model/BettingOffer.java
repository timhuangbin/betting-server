package com.betting.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BettingOffer {
    private final Map<Integer, Integer> customerMaxStakes;
    private volatile String cachedHighStakes;

    public BettingOffer() {
        this.customerMaxStakes = new ConcurrentHashMap<>();
        this.cachedHighStakes = "";
    }

    public void addStake(int customerId, int stake) {
        customerMaxStakes.merge(customerId, stake, Math::max);
        invalidateCache();
    }

    public String getHighStakes(int limit) {
        if (cachedHighStakes.isEmpty()) {
            cachedHighStakes = buildHighStakes(limit);
        }
        return cachedHighStakes;
    }

    private String buildHighStakes(int limit) {
        if (customerMaxStakes.isEmpty()) {
            return "";
        }

        return customerMaxStakes.entrySet().stream()
                .map(entry -> new CustomerStake(entry.getKey(), entry.getValue()))
                .sorted()
                .limit(limit)
                .map(CustomerStake::toString)
                .collect(Collectors.joining(","));
    }

    private void invalidateCache() {
        this.cachedHighStakes = "";
    }

    public void cleanupExcessData(int limit) {
        if (customerMaxStakes.size() <= limit) {
            return;
        }

        var topCustomers = customerMaxStakes.entrySet().stream()
                .map(entry -> new CustomerStake(entry.getKey(), entry.getValue()))
                .sorted()
                .limit(limit)
                .map(CustomerStake::getCustomerId)
                .collect(Collectors.toList());

        customerMaxStakes.keySet().retainAll(topCustomers);
        invalidateCache();
    }
}
