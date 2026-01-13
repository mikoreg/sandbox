package com.github.mikoreg.timelineaudio.speech;

import java.nio.file.Path;

public record SpeechConfig(
        String language,
        double speechSpeed,
        double charactersPerSecond,
        Path outputDirectory
) {
}
