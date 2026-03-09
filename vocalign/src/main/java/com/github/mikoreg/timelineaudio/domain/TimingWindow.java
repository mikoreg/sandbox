package com.github.mikoreg.timelineaudio.domain;

public record TimingWindow(long startMs, Long endMs) {
    public Long allowedMs() {
        if (endMs == null) {
            return null;
        }
        return Math.max(0L, endMs - startMs);
    }
}
