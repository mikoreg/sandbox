package com.github.mikoreg.timelineaudio.speech;

import com.github.mikoreg.timelineaudio.domain.SpeechSegment;
import com.github.mikoreg.timelineaudio.domain.SubtitleSegment;
import java.nio.file.Path;
import java.lang.System.Logger.Level;

public class MockSpeechSynthesizer implements SpeechSynthesizer {
    private final SpeechConfig config;
    private static final System.Logger LOG = System.getLogger(MockSpeechSynthesizer.class.getName());

    public MockSpeechSynthesizer(SpeechConfig config) {
        this.config = config;
    }

    @Override
    public SpeechSegment synthesize(SubtitleSegment subtitle) {
        String text = subtitle.text();
        double chars = Math.max(1, text.length());
        double durationSeconds = chars / config.charactersPerSecond();
        long durationMs = Math.max(200L, Math.round(durationSeconds * 1000.0));
        Path outputPath = config.outputDirectory().resolve(subtitle.id() + ".wav");
        LOG.log(Level.DEBUG, "Synthesizing mock audio for " + subtitle.id() + " duration " + durationMs + " ms");
        try {
            WavToneWriter.writeTone(outputPath, durationMs, 440.0, 0.2);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to write mock audio file for " + subtitle.id() + ": " + e.getMessage());
        }
        return new SpeechSegment(subtitle.id(), text, outputPath, durationMs);
    }
}
