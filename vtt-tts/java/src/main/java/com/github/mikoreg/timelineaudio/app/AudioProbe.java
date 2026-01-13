package com.github.mikoreg.timelineaudio.app;

import com.github.mikoreg.timelineaudio.domain.DebugLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AudioProbe {
    private final DebugLogger logger;

    public AudioProbe(DebugLogger logger) {
        this.logger = logger;
    }

    public long probeDurationMs(Path audioPath) {
        List<String> command = new ArrayList<>();
        command.add("ffprobe");
        command.add("-v");
        command.add("error");
        command.add("-show_entries");
        command.add("format=duration");
        command.add("-of");
        command.add("default=nk=1:nw=1");
        command.add(audioPath.toString());

        logger.debug("Probing audio duration via ffprobe: " + String.join(" ", command));
        try {
            Process process = new ProcessBuilder(command).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                int exit = process.waitFor();
                if (exit != 0) {
                    throw new IllegalStateException("ffprobe failed with exit code " + exit);
                }
                if (line == null || line.isBlank()) {
                    return 0L;
                }
                double seconds = Double.parseDouble(line.trim());
                return Math.max(0L, Math.round(seconds * 1000.0));
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("ffprobe failed", e);
        }
    }
}
