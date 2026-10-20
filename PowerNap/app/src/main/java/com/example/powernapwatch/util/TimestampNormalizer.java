package com.example.powernapwatch.util;

/** Maps an arbitrary monotonic-or-epoch millisecond sensor clock onto wall-clock time. */
public final class TimestampNormalizer {
    private boolean initialized;
    private long offsetMs;

    public synchronized long normalize(long sourceTimestampMs) {
        long now = System.currentTimeMillis();
        if (!initialized) {
            // If it already looks like Unix epoch time, preserve it.
            if (Math.abs(now - sourceTimestampMs) < 86_400_000L) offsetMs = 0;
            else offsetMs = now - sourceTimestampMs;
            initialized = true;
        }
        return sourceTimestampMs + offsetMs;
    }
}
