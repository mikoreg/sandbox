package com.github.mikoreg.timelineaudio.app;

import com.github.mikoreg.timelineaudio.domain.DebugLogger;
import com.github.mikoreg.timelineaudio.domain.SpeechSegment;
import com.github.mikoreg.timelineaudio.domain.TimingDecision;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SegmentAudioProcessor {
    private final DebugLogger logger;
    private final AudioProbe probe;

    public SegmentAudioProcessor(DebugLogger logger, AudioProbe probe) {
        this.logger = logger;
        this.probe = probe;
    }

    public SpeechSegment process(SpeechSegment speech, TimingDecision decision, Path outputDir) {
        Path normalizedWav = outputDir.resolve(speech.id() + ".wav");
        Path input = speech.audioPath();
        FilesUtil.ensureDirectory(outputDir);

        Path current = convertToWav(input, normalizedWav);
        if (decision.speedFactor() != 1.0) {
            current = applySpeed(current, outputDir.resolve(speech.id() + ".spd.wav"), decision.speedFactor());
        }
        if (decision.trimToMs() != null) {
            current = trimToLength(current, outputDir.resolve(speech.id() + ".trim.wav"), decision.trimToMs());
        }

        long durationMs = probe.probeDurationMs(current);
        return new SpeechSegment(speech.id(), speech.text(), current, durationMs);
    }

    private Path convertToWav(Path input, Path output) {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y");
        command.add("-i");
        command.add(input.toString());
        command.add("-ar");
        command.add("24000");
        command.add("-ac");
        command.add("1");
        command.add(output.toString());

        run(command, "normalize to wav");
        return output;
    }

    private Path applySpeed(Path input, Path output, double speed) {
        String filterSpec = buildAtempoFilter(speed);
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y");
        command.add("-i");
        command.add(input.toString());
        command.add("-filter:a");
        command.add(filterSpec);
        command.add("-ar");
        command.add("24000");
        command.add("-ac");
        command.add("1");
        command.add(output.toString());

        run(command, "apply speed");
        return output;
    }

    private Path trimToLength(Path input, Path output, long trimToMs) {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y");
        command.add("-i");
        command.add(input.toString());
        command.add("-t");
        command.add(String.valueOf(trimToMs / 1000.0));
        command.add("-ar");
        command.add("24000");
        command.add("-ac");
        command.add("1");
        command.add(output.toString());

        run(command, "trim segment");
        return output;
    }

    private String buildAtempoFilter(double speed) {
        if (speed <= 0) {
            throw new IllegalArgumentException("Speed must be > 0");
        }
        if (speed >= 0.5 && speed <= 2.0) {
            return "atempo=" + speed;
        }
        List<String> parts = new ArrayList<>();
        double remaining = speed;
        while (remaining > 2.0) {
            parts.add("atempo=2.0");
            remaining /= 2.0;
        }
        while (remaining < 0.5) {
            parts.add("atempo=0.5");
            remaining *= 2.0;
        }
        parts.add("atempo=" + remaining);
        return String.join(",", parts);
    }

    private void run(List<String> command, String label) {
        logger.debug("ffmpeg " + label + ": " + String.join(" ", command));
        try {
            Process process = new ProcessBuilder(command).inheritIO().start();
            int exit = process.waitFor();
            if (exit != 0) {
                throw new IllegalStateException("ffmpeg failed with exit code " + exit);
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("ffmpeg execution failed", e);
        }
    }

    private static class FilesUtil {
        static void ensureDirectory(Path path) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create directory " + path, e);
            }
        }
    }
}
