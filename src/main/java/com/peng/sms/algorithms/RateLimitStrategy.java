package com.peng.sms.algorithms;

public interface RateLimitStrategy {
    String getName();
    // For local storage, logic is handled here.

    // For Redis, this usually identifies which Lua script to load.

}