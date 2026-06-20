package com.chitfund.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Map<String, Deque<Instant>> attemptsByKey = new ConcurrentHashMap<>();

    public boolean allow(String key) {
        Instant now = Instant.now();
        Deque<Instant> attempts = attemptsByKey.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (attempts) {
            while (!attempts.isEmpty() && attempts.peekFirst().plus(WINDOW).isBefore(now)) {
                attempts.removeFirst();
            }

            if (attempts.size() >= MAX_ATTEMPTS) {
                return false;
            }

            attempts.addLast(now);
            return true;
        }
    }
}
