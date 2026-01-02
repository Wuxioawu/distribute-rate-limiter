package com.peng.sms.algorithms;

import java.util.concurrent.atomic.AtomicLong;

public class TokenBucket implements RateLimiter {

    private final long capacity; // maximum number of tokens
    private final long refillRate; // tokens added per second
    private final AtomicLong tokens; // current number of tokens
    private long lastRefillTime;  // last time the bucket was refilled

    public TokenBucket(int capacity, int refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = new AtomicLong(refillRate);
        this.lastRefillTime = System.currentTimeMillis();
    }

    private synchronized void refill() {
        long now = System.currentTimeMillis();
        long duration = now - lastRefillTime;
        long tokensToAdd = (duration * refillRate) / 1000;
        if(tokensToAdd > 0) {
            tokens.updateAndGet(current -> Math.min(capacity, current + tokensToAdd));
            lastRefillTime = now;
        }
    }

    @Override
    public boolean allow() {
        refill();
        long remaining = tokens.getAndUpdate(current -> current > 0 ? current - 1 : current);
        return remaining > 0;
    }
}
