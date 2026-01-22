package com.github.mikoreg.timelineaudio.app;

import java.nio.file.Path;

public record AppConfig(
        Path inputPath,
        Path outputPath,
        Path speechOutputDir,
        Path processedOutputDir,
        String renderer,
        String encodeCodec,
        String ttsProvider,
        String language,
        boolean allowStretchSegments,
        int stretchSegmentLimit,
        boolean debug,
        long previewMs,
        double speechSpeed,
        double charactersPerSecond,
        long minTrimMs,
        double maxSpeedFactor,
        double frameRate,
        String ffmpegLogLevelOverride
) {
}
