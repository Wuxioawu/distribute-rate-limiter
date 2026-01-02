package com.peng.sms.algorithms;

public class LeakBucket implements RateLimiter {
    private final long capacity;
    private final long leakRate; // tokens per second

    private long water;
    private long lastLeakTime;

    public LeakBucket(long capacity, long leakRate) {
        this.capacity = capacity;
        this.leakRate = leakRate;
        this.lastLeakTime = System.currentTimeMillis();
        this.water = 0; // Initialize empty
    }

    @Override
    public boolean allow() {
        leak();
        if (water < capacity) {
            water++;
            return true;
        }
        return false;
    }

    private void leak() {
        long now = System.currentTimeMillis();
        long duration = now - lastLeakTime;

        // Calculate the amount of water to leak: (milliseconds * rate) / 1000
        long leakedWater = (duration * leakRate) / 1000;

        if (leakedWater > 0) {
            // Subtract leaked water but ensure it doesn't go below zero
            water = Math.max(0, water - leakedWater);
            lastLeakTime = now;
        }
    }
}
