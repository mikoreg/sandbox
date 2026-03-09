package com.github.mikoreg.timelineaudio.subtitles;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class TtmlTimecodeParser {
    private static final DateTimeFormatter FORMAT_WITH_MS = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final DateTimeFormatter FORMAT_NO_MS = DateTimeFormatter.ofPattern("HH:mm:ss");

    private TtmlTimecodeParser() {
    }

    public static long parseToMs(String timecode) {
        try {
            LocalTime time = LocalTime.parse(timecode, FORMAT_WITH_MS);
            return Duration.between(LocalTime.MIDNIGHT, time).toMillis();
        } catch (DateTimeParseException ignored) {
        }
        LocalTime time = LocalTime.parse(timecode, FORMAT_NO_MS);
        return Duration.between(LocalTime.MIDNIGHT, time).toMillis();
    }
}
