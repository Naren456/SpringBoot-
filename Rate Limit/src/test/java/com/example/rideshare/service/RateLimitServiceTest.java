package com.example.rideshare.service;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitServiceTest {

    @Test
    void testRateLimit() {
        RateLimitService service = new RateLimitService();
        String key = "test-user";
        
        // 5 requests allowed
        assertTrue(service.allowRequest(key));
        assertTrue(service.allowRequest(key));
        assertTrue(service.allowRequest(key));
        assertTrue(service.allowRequest(key));
        assertTrue(service.allowRequest(key));
        
        // 6th blocked
        assertFalse(service.allowRequest(key));
    }

    @Test
    void testConcurrency() throws InterruptedException {
        RateLimitService service = new RateLimitService();
        String key = "concurrent-user";
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger allowedCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    if (service.allowRequest(key)) {
                        allowedCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        latch.countDown(); // Start all threads
        executor.shutdown();
        Thread.sleep(100); // Wait for finish

        // Should be exactly 5
        assertEquals(5, allowedCount.get());
    }
}
