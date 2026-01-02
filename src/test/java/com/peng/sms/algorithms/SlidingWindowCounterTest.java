package com.peng.sms.algorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SlidingWindowCounterTest {
    @Test
    @DisplayName("Verify sliding window boundary logic")
    void testSlidingWindow() throws InterruptedException {
        // Limit: 10 requests per 1000ms, split into 5 buckets (200ms each)
        SlidingWindowCounter limiter = new SlidingWindowCounter(10, 1000, 5);

        // 1. Fill the window
        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(limiter.allow());
        }
        Assertions.assertFalse(limiter.allow(), "Should be limited after 10 requests");

        // 2. Wait for 1 bucket (200ms) to slide out
        Thread.sleep(250);

        // 3. Should now allow at least one request as the oldest bucket is cleared
        Assertions.assertTrue(limiter.allow(), "Should allow request after window slides");
    }
}