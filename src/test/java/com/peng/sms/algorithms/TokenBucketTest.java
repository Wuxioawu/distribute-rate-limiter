package com.peng.sms.algorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


class RateLimiterTest {
    @Test
    @DisplayName("Test basic rate limiting and bucket exhaustion")
    void testBasicLimiting() throws InterruptedException {
        // Capacity 5, Refill 1 per second
        TokenBucket limiter = new TokenBucket(5, 5);
        // 1. Consume all 5 initial tokens
        for (int i = 1; i <= 5; i++) {
            Assertions.assertTrue(limiter.allow(), "Request " + (i + 1) + " should be allowed");
        }

        // 2. The 6th request should fail immediately
        Assertions.assertFalse(limiter.allow(), "The 6th request should be blocked");
    }

    @Test
    @DisplayName("Test token refill after waiting")
    void testRefillLogic() throws InterruptedException {
        // Capacity 2, Refill 2 per second
        TokenBucket limiter = new TokenBucket(2, 2);

        // Exhaust the bucket
        limiter.allow();
        limiter.allow();
        Assertions.assertFalse(limiter.allow());

        // Wait 1.1 seconds (enough to refill 2 tokens)
        Thread.sleep(1100);

        // Should be able to allow 2 more requests now
        Assertions.assertTrue(limiter.allow(), "Should have refilled tokens");
        Assertions.assertTrue(limiter.allow(), "Should have refilled tokens");
    }

    @Test
    @DisplayName("Test high concurrency (Thread Safety)")
    void testConcurrency() throws InterruptedException {
        int capacity = 100;
        int threads = 50;
        TokenBucket limiter = new TokenBucket(capacity, 10);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);

        // Fire 200 requests across 50 threads simultaneously
        for (int i = 0; i < 200; i++) {
            executor.submit(() -> {
                if (limiter.allow()) {
                    successCount.incrementAndGet();
                }
            });
        }

        latch.await(2, java.util.concurrent.TimeUnit.SECONDS);
        executor.shutdown();

        // Even with 200 requests, only 100 (initial capacity)
        // plus a few refilled ones should succeed.
        Assertions.assertTrue(successCount.get() >= 100 && successCount.get() < 120,
                "Success count " + successCount.get() + " should be close to capacity");
    }
}