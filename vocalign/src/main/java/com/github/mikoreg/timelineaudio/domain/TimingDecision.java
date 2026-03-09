package com.github.mikoreg.timelineaudio.domain;

public record TimingDecision(double speedFactor, Long trimToMs, boolean allowOverflow) {
}
