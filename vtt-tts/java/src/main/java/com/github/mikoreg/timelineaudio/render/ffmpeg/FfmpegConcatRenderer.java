package com.github.mikoreg.timelineaudio.render.ffmpeg;

import com.github.mikoreg.timelineaudio.domain.DebugLogger;
import com.github.mikoreg.timelineaudio.domain.RenderJob;
import com.github.mikoreg.timelineaudio.domain.RenderResult;
import com.github.mikoreg.timelineaudio.domain.RenderSegment;
import com.github.mikoreg.timelineaudio.render.AudioRenderer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FfmpegConcatRenderer implements AudioRenderer {
    private final DebugLogger logger;

    public FfmpegConcatRenderer(DebugLogger logger) {
        this.logger = logger;
    }

    @Override
    public RenderResult render(RenderJob job) {
        List<RenderSegment> sorted = job.segments().stream()
                .sorted(Comparator.comparingLong(RenderSegment::startMs))
                .toList();
        Path outputPath = job.outputPath();
        long timelineMs = job.timelineDurationMs();

        logger.info("Rendering timeline via ffmpeg concat to " + outputPath);
        try {
            Files.createDirectories(outputPath.getParent());
            Path tempDir = Files.createTempDirectory(outputPath.getParent(), "concat_");
            List<Path> concatParts = new ArrayList<>();

            long cursorMs = 0L;
            int silenceIndex = 0;
            for (RenderSegment segment : sorted) {
                long gap = segment.startMs() - cursorMs;
                if (gap > 0) {
                    Path silence = tempDir.resolve("silence_" + silenceIndex + ".wav");
                    createSilence(silence, gap);
                    concatParts.add(silence);
                    cursorMs += gap;
                    silenceIndex++;
                }
                concatParts.add(segment.audioPath());
                cursorMs += segment.durationMs();
            }

            if (timelineMs > cursorMs) {
                Path tail = tempDir.resolve("silence_tail.wav");
                createSilence(tail, timelineMs - cursorMs);
                concatParts.add(tail);
            }

            Path listFile = tempDir.resolve("concat_list.txt");
            writeConcatList(listFile, concatParts);
            runFfmpegConcat(listFile, outputPath);

            return new RenderResult(outputPath, timelineMs, sorted.size());
        } catch (IOException e) {
            logger.error("Failed to render via ffmpeg concat", e);
            throw new IllegalStateException("ffmpeg concat rendering failed", e);
        }
    }

    private void createSilence(Path output, long durationMs) {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y");
        command.add("-f");
        command.add("lavfi");
        command.add("-i");
        command.add("anullsrc=r=24000:cl=mono");
        command.add("-t");
        command.add(String.valueOf(durationMs / 1000.0));
        command.add("-ar");
        command.add("24000");
        command.add("-ac");
        command.add("1");
        command.add(output.toString());

        run(command, "create silence");
    }

    private void writeConcatList(Path listFile, List<Path> parts) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(listFile)) {
            for (Path part : parts) {
                String absolute = part.toAbsolutePath().toString();
                writer.write("file '" + absolute.replace("'", "'\\''") + "'\n");
            }
        }
    }

    private void runFfmpegConcat(Path listFile, Path outputPath) {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y");
        command.add("-f");
        command.add("concat");
        command.add("-safe");
        command.add("0");
        command.add("-i");
        command.add(listFile.toString());
        command.add("-c:a");
        command.add("pcm_s16le");
        command.add(outputPath.toString());

        run(command, "concat timeline");
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
}
