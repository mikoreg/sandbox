package com.github.mikoreg.timelineaudio.app;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FfmpegEncoder {
    private static final System.Logger LOG = System.getLogger(FfmpegEncoder.class.getName());
    private final String ffmpegLogLevel;

    public FfmpegEncoder(String ffmpegLogLevel) {
        this.ffmpegLogLevel = ffmpegLogLevel;
    }

    public Path encode(Path inputWav, String codec) {
        String normalized = codec.toLowerCase();
        String extension = switch (normalized) {
            case "opus" -> "opus";
            case "aac" -> "m4a";
            case "mp3" -> "mp3";
            default -> throw new IllegalArgumentException("Unsupported codec: " + codec);
        };
        Path output = inputWav.resolveSibling(stripExtension(inputWav.getFileName().toString()) + "." + extension);

        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-loglevel");
        command.add(ffmpegLogLevel);
        command.add("-y");
        command.add("-i");
        command.add(inputWav.toString());

        switch (normalized) {
            case "opus" -> {
                command.add("-c:a");
                command.add("libopus");
                command.add("-b:a");
                command.add("64k");
            }
            case "aac" -> {
                command.add("-c:a");
                command.add("aac");
                command.add("-b:a");
                command.add("96k");
            }
            case "mp3" -> {
                command.add("-c:a");
                command.add("libmp3lame");
                command.add("-q:a");
                command.add("2");
            }
            default -> {
            }
        }

        command.add(output.toString());

        LOG.log(Level.INFO, "Encoding audio via ffmpeg: " + String.join(" ", command));
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
            throw new IllegalStateException("ffmpeg encoding failed", e);
        }
        return output;
    }

    private String stripExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx <= 0) {
            return filename;
        }
        return filename.substring(0, idx);
    }
}
