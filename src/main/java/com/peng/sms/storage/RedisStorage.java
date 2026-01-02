package com.peng.sms.storage;


import com.peng.sms.algorithms.RateLimitStrategy;
import com.peng.sms.core.LimitRule;

public class RedisStorage implements RateLimitStorage {

    @Override
    public boolean checkAndIncrement(String key, LimitRule rule, RateLimitStrategy strategy) {
        return false;
    }
}