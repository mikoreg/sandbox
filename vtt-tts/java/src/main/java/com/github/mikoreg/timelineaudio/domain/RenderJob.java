package com.github.mikoreg.timelineaudio.domain;

import java.nio.file.Path;
import java.util.List;

public record RenderJob(List<RenderSegment> segments, Path outputPath, long timelineDurationMs, double frameRate) {
}
