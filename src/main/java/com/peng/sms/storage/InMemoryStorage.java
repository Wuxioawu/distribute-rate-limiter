package com.peng.sms.storage;


import com.peng.sms.algorithms.RateLimitStrategy;
import com.peng.sms.core.LimitRule;

// In-memory storage implementation for rate limiting
public class InMemoryStorage implements RateLimitStorage {

    @Override
    public boolean checkAndIncrement(String key, LimitRule rule, RateLimitStrategy strategy) {
        return false;
    }
}