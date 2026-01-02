package com.peng.sms.algorithms;

public interface RateLimiter {
    // Returns true if the request is allowed, false otherwise
    boolean allow();
}