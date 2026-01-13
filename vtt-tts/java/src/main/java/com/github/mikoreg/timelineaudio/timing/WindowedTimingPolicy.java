package com.github.mikoreg.timelineaudio.timing;

import com.github.mikoreg.timelineaudio.domain.DebugLogger;
import com.github.mikoreg.timelineaudio.domain.SpeechSegment;
import com.github.mikoreg.timelineaudio.domain.SubtitleSegment;
import com.github.mikoreg.timelineaudio.domain.TimingDecision;
import com.github.mikoreg.timelineaudio.domain.TimingWindow;

public class WindowedTimingPolicy implements TimingPolicy {
    private final TimingConfig config;
    private final DebugLogger logger;

    public WindowedTimingPolicy(TimingConfig config, DebugLogger logger) {
        this.config = config;
        this.logger = logger;
    }

    @Override
    public TimingDecision decide(SubtitleSegment subtitle, SpeechSegment speech, TimingWindow window) {
        Long allowedMs = window.allowedMs();
        long actualMs = speech.durationMs();
        if (allowedMs == null || allowedMs <= 0) {
            logger.debug("No timing window available for " + subtitle.id() + "; using base speed " + config.defaultSpeechSpeed());
            return new TimingDecision(config.defaultSpeechSpeed(), null, true);
        }

        if (actualMs <= allowedMs) {
            logger.debug("Segment " + subtitle.id() + " fits in window " + allowedMs + " ms; applying base speed " + config.defaultSpeechSpeed());
            return new TimingDecision(config.defaultSpeechSpeed(), null, false);
        }

        if (allowedMs < config.minTrimMs()) {
            logger.warn("Segment " + subtitle.id() + " window " + allowedMs + " ms below min trim; allowing overflow.");
            return new TimingDecision(config.defaultSpeechSpeed(), null, true);
        }

        double factor = (double) actualMs / allowedMs;
        double capped = Math.max(config.defaultSpeechSpeed(), Math.min(factor, config.maxSpeedFactor()));
        logger.info("Segment " + subtitle.id() + " needs speed factor " + capped + " to fit in " + allowedMs + " ms.");
        return new TimingDecision(capped, allowedMs, false);
    }
}
