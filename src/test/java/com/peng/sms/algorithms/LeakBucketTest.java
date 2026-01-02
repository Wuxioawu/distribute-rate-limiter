package com.peng.sms.algorithms;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

class LeakBucketTest {
    @Test
    @DisplayName("Verify that bucket rejects requests when full")
    void testBucketCapacity() {
        // Capacity 5, Leak Rate 1 per second
        LeakBucket limiter = new LeakBucket(5, 1);

        // Fill the bucket
        for (int i = 0; i < 5; i++) {
            Assertions.assertTrue(limiter.allow(), "Request " + (i + 1) + " should be allowed");
        }

        // The 6th request should fail because the bucket is full
        Assertions.assertFalse(limiter.allow(), "The 6th request should be rejected");
    }

    @Test
    @DisplayName("Verify that water leaks out over time")
    void testLeakingLogic() throws InterruptedException {
        // Capacity 2, Leak Rate 2 per second
        LeakBucket limiter = new LeakBucket(2, 2);

        // Fill the bucket to capacity
        limiter.allow();
        limiter.allow();
        Assertions.assertFalse(limiter.allow(), "Bucket should be full");

        // Wait 1.1 seconds. At a rate of 2/sec, the bucket should be empty now.
        Thread.sleep(1100);

        // Should be able to add 2 more requests
        Assertions.assertTrue(limiter.allow(), "Should allow request after leak");
        Assertions.assertTrue(limiter.allow(), "Should allow second request after leak");
    }

    @Test
    @DisplayName("Test Concurrency - High Volume")
    void testConcurrency() throws InterruptedException {
        int threads = 50;
        int capacity = 100;
        // High leak rate to see if it handles simultaneous leak/add
        LeakBucket limiter = new LeakBucket(capacity, 10);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(200);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < 200; i++) {
            executor.submit(() -> {
                try {
                    if (limiter.allow()) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        System.out.println("Successful requests: " + successCount.get());
        // Since we fired 200 requests instantly at a capacity of 100,
        // the success count should be exactly 100 (plus very few leaks during the ms of execution)
        Assertions.assertTrue(successCount.get() >= 100 && successCount.get() <= 105,
                "Success count was " + successCount.get());
    }
}