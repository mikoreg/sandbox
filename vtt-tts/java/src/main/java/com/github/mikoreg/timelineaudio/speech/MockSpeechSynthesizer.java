package com.github.mikoreg.timelineaudio.speech;

import com.github.mikoreg.timelineaudio.domain.DebugLogger;
import com.github.mikoreg.timelineaudio.domain.SpeechSegment;
import com.github.mikoreg.timelineaudio.domain.SubtitleSegment;

import java.nio.file.Path;

public class MockSpeechSynthesizer implements SpeechSynthesizer {
    private final SpeechConfig config;
    private final DebugLogger logger;

    public MockSpeechSynthesizer(SpeechConfig config, DebugLogger logger) {
        this.config = config;
        this.logger = logger;
    }

    @Override
    public SpeechSegment synthesize(SubtitleSegment subtitle) {
        String text = subtitle.text();
        double chars = Math.max(1, text.length());
        double durationSeconds = chars / config.charactersPerSecond();
        long durationMs = Math.max(200L, Math.round(durationSeconds * 1000.0));
        Path outputPath = config.outputDirectory().resolve(subtitle.id() + ".wav");
        logger.debug("Synthesizing mock audio for " + subtitle.id() + " duration " + durationMs + " ms");
        try {
            WavToneWriter.writeTone(outputPath, durationMs, 440.0, 0.2);
        } catch (Exception e) {
            logger.warn("Failed to write mock audio file for " + subtitle.id() + ": " + e.getMessage());
        }
        return new SpeechSegment(subtitle.id(), text, outputPath, durationMs);
    }
}
