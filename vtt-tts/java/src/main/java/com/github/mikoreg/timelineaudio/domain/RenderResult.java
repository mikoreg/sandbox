package com.github.mikoreg.timelineaudio.domain;

import java.nio.file.Path;

public record RenderResult(Path outputPath, long timelineDurationMs, int segmentsRendered) {
}
