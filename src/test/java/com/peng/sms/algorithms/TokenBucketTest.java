package com.peng.sms.algorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
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
    @DisplayName("Verify thread safety under high concurrency")
    void testConcurrency() throws InterruptedException {
        int capacity = 100;
        int threads = 10;
        int totalRequests = 200;

        // Initialize bucket with capacity of 100
        TokenBucket limiter = new TokenBucket(capacity, 10);

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        // FinishLatch: Ensures main thread waits for all 200 requests to complete
        CountDownLatch finishLatch = new CountDownLatch(totalRequests);

        // StartBarrier: Synchronizes the 50 threads to start at the exact same time
        CyclicBarrier startBarrier = new CyclicBarrier(threads);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    // Synchronize the first 50 threads to create a "burst" effect
                    if (Thread.activeCount() <= threads) {
                        try {
                            startBarrier.await(1, TimeUnit.SECONDS);
                        } catch (Exception ignored) {}
                    }

                    if (limiter.allow()) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    // Always count down so the test doesn't hang forever
                    finishLatch.countDown();
                }
            });
        }

        // Wait for all threads to finish their work (up to 5 seconds)
        boolean completed = finishLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.println("Final successful requests: " + successCount.get());

        // Assertions
        Assertions.assertTrue(completed, "The test timed out before all requests finished");

        // Success count should be at least the initial capacity (100)
        // plus maybe 1 or 2 extra tokens refilled during the few milliseconds of execution.
        Assertions.assertTrue(successCount.get() >= 10 && successCount.get() <= 20,
                "Success count " + successCount.get() + " should be strictly limited by capacity");
    }
}