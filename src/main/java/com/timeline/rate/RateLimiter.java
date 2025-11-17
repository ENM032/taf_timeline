package com.timeline.rate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiter {
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final int limit;
    private final long windowMillis;

    public RateLimiter(int limitPerWindow, long windowMillis) {
        this.limit = limitPerWindow;
        this.windowMillis = windowMillis;
    }

    public boolean allow(String key) {
        long now = System.currentTimeMillis();
        Window w = windows.compute(key, (k, existing) -> {
            if (existing == null || now - existing.start >= windowMillis) {
                return new Window(now);
            }
            return existing;
        });
        int v = w.count.incrementAndGet();
        return v <= limit;
    }

    private static class Window {
        final long start;
        final AtomicInteger count = new AtomicInteger(0);
        Window(long s) { this.start = s; }
    }
}