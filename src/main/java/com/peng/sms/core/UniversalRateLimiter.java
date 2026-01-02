package com.peng.sms.core;


import com.peng.sms.algorithms.RateLimitStrategy;
import com.peng.sms.storage.RateLimitStorage;

public class UniversalRateLimiter implements RateLimiter {
    private final RateLimitStorage storage;
    private final RateLimitStrategy strategy;

    public UniversalRateLimiter(RateLimitStorage storage, RateLimitStrategy strategy) {
        this.storage = storage;
        this.strategy = strategy;
    }

    @Override
    public boolean tryAcquire(String key, LimitRule rule) {
        return storage.checkAndIncrement(key, rule, strategy);
    }
}