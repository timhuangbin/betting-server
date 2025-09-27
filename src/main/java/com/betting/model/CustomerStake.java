package com.betting.model;

public class CustomerStake implements Comparable<CustomerStake> {
    private final int customerId;
    private final int stake;

    public CustomerStake(int customerId, int stake) {
        this.customerId = customerId;
        this.stake = stake;
    }

    public int getCustomerId() {
        return customerId;
    }

    @Override
    public int compareTo(CustomerStake other) {
        return Integer.compare(other.stake, this.stake); // 降序排序
    }

    @Override
    public String toString() {
        return customerId + "=" + stake;
    }
}
