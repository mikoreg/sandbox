package com.github.mikoreg.timelineaudio.domain;

import java.nio.file.Path;

public record SpeechSegment(String id, String text, Path audioPath, long durationMs) {
}
