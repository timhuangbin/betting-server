package com.betting.service;

import com.betting.store.BettingStore;

public class BettingService {
    private final BettingStore bettingStore;

    public BettingService(BettingStore bettingStore) {
        this.bettingStore = bettingStore;
    }

    public void addStake(int offerId, int customerId, int stake) {
        bettingStore.addStake(offerId, customerId, stake);
    }

    public String getHighStakes(int offerId) {
        return bettingStore.getHighStakes(offerId).orElse("");
    }
}
