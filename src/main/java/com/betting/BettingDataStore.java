package com.betting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BettingDataStore {
    // Storage Structure: offerId -> customerId -> maxStake
    private final Map<Integer, Map<Integer, Integer>> bettingData;

    public BettingDataStore() {
        this.bettingData = new ConcurrentHashMap<>();
    }

    public void addStake(int offerId, int customerId, int stake) {
        bettingData.compute(offerId, (k, v) -> {
            if (v == null) {
                v = new ConcurrentHashMap<>();
            }

            // Update the customer's maximum stake amount
            v.merge(customerId, stake, Math::max);
            return v;
        });
    }

    public String getHighStakes(int offerId) {
        Map<Integer, Integer> offerStakes = bettingData.get(offerId);
        if (offerStakes == null || offerStakes.isEmpty()) {
            return "";
        }

        // Create a list of customer IDs and maximum stake amounts
        List<Map.Entry<Integer, Integer>> stakesList = new ArrayList<>(offerStakes.entrySet());

        // Sort by stake amount in descending order
        stakesList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Take the top 20
        int limit = Math.min(20, stakesList.size());
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < limit; i++) {
            Map.Entry<Integer, Integer> entry = stakesList.get(i);
            if (i > 0) {
                result.append(",");
            }
            result.append(entry.getKey()).append("=").append(entry.getValue());
        }

        return result.toString();
    }
}
