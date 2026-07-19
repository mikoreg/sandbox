package com.github.mikoreg.timelineaudio.domain;

import java.nio.file.Path;

public record RenderSegment(String id, Path audioPath, long startMs, long durationMs, double speedFactor) {
}
