package com.peng.sms.algorithms;

import java.util.concurrent.atomic.AtomicInteger;

public class SlidingWindowCounter implements RateLimiter {
    private final int limit;                // Max requests allowed across the whole window
    private final int windowSizeInMs;       // Total duration of the window (e.g., 1000ms)
    private final int bucketCount;          // Number of sub-windows (buckets)
    private final AtomicInteger[] buckets;  // Array to store counts for each sub-window
    private final int bucketSizeInMs;       // Duration of each sub-window
    private long lastWindowTime;            // Last time the window was checked/reset

    public SlidingWindowCounter(int limit, int windowSizeInMs, int bucketCount) {
        this.limit = limit;
        this.windowSizeInMs = windowSizeInMs;
        this.bucketCount = bucketCount;
        this.buckets = new AtomicInteger[bucketCount];
        for (int i = 0; i < bucketCount; i++) {
            this.buckets[i] = new AtomicInteger(0);
        }
        this.bucketSizeInMs = windowSizeInMs / bucketCount;
        this.lastWindowTime = System.currentTimeMillis();
    }

    @Override
    public boolean allow() {
        long now = System.currentTimeMillis();
        // Determine which sub-window (bucket) the current time falls into
        int currentIndex = (int) ((now % windowSizeInMs) / bucketSizeInMs);
        // Clean up expired buckets based on elapsed time
        resetOldBuckets(now);

        // Sum up the counts across all buckets in the current sliding window
        int currentTotalCount = 0;
        for (AtomicInteger bucket : buckets) {
            currentTotalCount += bucket.get();
        }

        // Check if we are under the limit
        if (currentTotalCount < limit) {
            buckets[currentIndex].incrementAndGet();
            return true;
        }

        return false;
    }

    /**
     * Resets buckets that fall outside the current time window.
     */
    private void resetOldBuckets(long now) {
        // Calculate how many buckets have "passed" since the last check
        long timePassed = now - lastWindowTime;
        int bucketsToClear = (int) (timePassed / bucketSizeInMs);

        if (bucketsToClear > 0) {
            for (int i = 1; i <= bucketsToClear && i <= bucketCount; i++) {
                // Circular index to clear buckets in the future/past
                int indexToClear = (int) (((lastWindowTime / bucketSizeInMs) + i) % bucketCount);
                buckets[indexToClear].set(0);
            }
            lastWindowTime = now;
        }
    }
}
