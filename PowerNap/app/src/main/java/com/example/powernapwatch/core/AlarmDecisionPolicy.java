package com.example.powernapwatch.core;

/** Pure-Java policy for choosing between the normal nap alarm and the hard deadline. */
public final class AlarmDecisionPolicy {
    public static final String SLEEP_DURATION = "SLEEP_DURATION";
    public static final String HARD_DEADLINE = "HARD_DEADLINE";

    private AlarmDecisionPolicy() { }

    public record Plan(long normalTargetMs, long hardTargetMs, long selectedAtMs, String reason) { }

    public static Plan choose(long sessionStartMs,
                              long estimatedSleepOnsetMs,
                              long nowMs,
                              long napDurationMs,
                              long maxTotalTimeMs) {
        long normal = estimatedSleepOnsetMs + napDurationMs;
        long hard = sessionStartMs + maxTotalTimeMs;

        if (nowMs >= hard) {
            // The deadline has already been reached; re-arm immediately.
            return new Plan(normal, hard, nowMs + 2_000L, HARD_DEADLINE);
        }

        if (normal < hard) {
            // If Health Services reports the onset retrospectively and the normal
            // target already passed, wake as soon as AlarmManager can be re-armed,
            // but never move the alarm beyond the still-future hard deadline.
            long selected = Math.min(Math.max(nowMs + 2_000L, normal), hard);
            String reason = selected >= hard ? HARD_DEADLINE : SLEEP_DURATION;
            return new Plan(normal, hard, selected, reason);
        }
        return new Plan(normal, hard, hard, HARD_DEADLINE);
    }
}
