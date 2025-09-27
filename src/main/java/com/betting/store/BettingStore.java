package com.betting.store;

import com.betting.model.BettingOffer;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class BettingStore {
    private final Map<Integer, BettingOffer> bettingOffers;
    private final Map<Integer, ReentrantLock> offerLocks;
    private final int highStakesLimit;

    public BettingStore(int highStakesLimit) {
        this.bettingOffers = new ConcurrentHashMap<>();
        this.offerLocks = new ConcurrentHashMap<>();
        this.highStakesLimit = highStakesLimit;
    }

    public void addStake(int offerId, int customerId, int stake) {
        ReentrantLock lock = offerLocks.computeIfAbsent(offerId, k -> new ReentrantLock());
        lock.lock();
        try {
            BettingOffer offer = bettingOffers.computeIfAbsent(offerId, k -> new BettingOffer());
            offer.addStake(customerId, stake);
            offer.cleanupExcessData(highStakesLimit);
        } finally {
            lock.unlock();
        }
    }

    public Optional<String> getHighStakes(int offerId) {
        BettingOffer offer = bettingOffers.get(offerId);
        if (offer == null) {
            return Optional.empty();
        }

        String highStakes = offer.getHighStakes(highStakesLimit);
        return Optional.of(highStakes);
    }
}
