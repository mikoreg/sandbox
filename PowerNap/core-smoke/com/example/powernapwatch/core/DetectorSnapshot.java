package com.example.powernapwatch.core;

public record DetectorSnapshot(
        long timestampMs,
        NapState state,
        double confidence,
        double baselineHeartRate,
        double currentHeartRate,
        double heartRateSlopeBpmPerSecond,
        double movementRms,
        double rmssdMs,
        long quietDurationMs,
        long candidateDurationMs,
        boolean onBody,
        String reason) {
}
