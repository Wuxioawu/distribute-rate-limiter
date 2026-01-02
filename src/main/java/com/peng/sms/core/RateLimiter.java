package com.peng.sms.core;

public interface RateLimiter {
    /**
     * @param key The unique identifier (e.g., user ID or IP)
     * @param rule The limitation rule to apply
     * @return true if the request is allowed, false otherwise
     */
    boolean tryAcquire(String key, LimitRule rule);
}
