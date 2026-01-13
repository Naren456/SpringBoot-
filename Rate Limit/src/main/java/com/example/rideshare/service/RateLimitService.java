package com.example.rideshare.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RateLimitService {

    private static final int LIMIT = 5;
    private static final int WINDOW_SECONDS = 60;

    private static class Counter {
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicLong windowStart = new AtomicLong(Instant.now().getEpochSecond());
    }

    private final Map<String, Counter> store = new ConcurrentHashMap<>();

    public boolean allowRequest(String key) {
        long now = Instant.now().getEpochSecond();
        
        Counter counter = store.computeIfAbsent(key, k -> new Counter());
        
        // Use atomic operations to ensure thread safety
        return checkWithAtomic(counter, now);
    }

    private boolean checkWithAtomic(Counter counter, long now) {
        while (true) {
            long start = counter.windowStart.get();
            if (now - start >= WINDOW_SECONDS) {
                 // Try to reset window
                 if (counter.windowStart.compareAndSet(start, now)) {
                     counter.count.set(1); // Reset count to 1 (current request)
                     return true;
                 }
                 // If failed, another thread updated window, loop again
            } else {
                int current = counter.count.get();
                if (current < LIMIT) {
                    if (counter.count.compareAndSet(current, current + 1)) {
                        return true;
                    }
                    // If failed, another thread updated count, loop again
                } else {
                    return false;
                }
            }
        }
    }
}
