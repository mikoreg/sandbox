package com.github.mikoreg.timelineaudio.render.mlt;

import com.github.mikoreg.timelineaudio.domain.RenderJob;
import com.github.mikoreg.timelineaudio.domain.RenderResult;
import com.github.mikoreg.timelineaudio.domain.RenderSegment;
import com.github.mikoreg.timelineaudio.render.AudioRenderer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class MltXmlRenderer implements AudioRenderer {
    private static final System.Logger LOG = System.getLogger(MltXmlRenderer.class.getName());

    public MltXmlRenderer() {}

    @Override
    public RenderResult render(RenderJob job) {
        List<RenderSegment> sorted = job.segments().stream()
                .sorted(Comparator.comparingLong(RenderSegment::startMs))
                .toList();
        Path outputPath = job.outputPath();
        double frameRate = job.frameRate();
        long timelineMs = job.timelineDurationMs();

        LOG.log(Level.INFO, "Rendering MLT XML to " + outputPath);
        try {
            Files.createDirectories(outputPath.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                writer.write("<mlt LC_NUMERIC=\"C\" version=\"7\">\n");
                writer.write("  <profile frame_rate_num=\"" + (int) frameRate + "\" frame_rate_den=\"1\" sample_rate=\"24000\" channels=\"1\"/>\n");

                int producerIndex = 0;
                for (RenderSegment segment : sorted) {
                    writer.write("  <producer id=\"producer-" + producerIndex + "\" in=\"0\" out=\"" + toFrames(segment.durationMs(), frameRate) + "\">\n");
                    writer.write("    <property name=\"resource\">" + escape(segment.audioPath().toString()) + "</property>\n");
                    writer.write("  </producer>\n");
                    producerIndex++;
                }

                writer.write("  <playlist id=\"audio-playlist\">\n");
                long currentMs = 0;
                for (int i = 0; i < sorted.size(); i++) {
                    RenderSegment segment = sorted.get(i);
                    if (segment.startMs() > currentMs) {
                        long gapMs = segment.startMs() - currentMs;
                        writer.write("    <blank length=\"" + toFrames(gapMs, frameRate) + "\"/>\n");
                        currentMs += gapMs;
                    }
                    writer.write("    <entry producer=\"producer-" + i + "\" in=\"0\" out=\"" + toFrames(segment.durationMs(), frameRate) + "\"/>\n");
                    currentMs += segment.durationMs();
                }
                if (timelineMs > currentMs) {
                    writer.write("    <blank length=\"" + toFrames(timelineMs - currentMs, frameRate) + "\"/>\n");
                }
                writer.write("  </playlist>\n");

                writer.write("  <tractor id=\"tractor-0\">\n");
                writer.write("    <track producer=\"audio-playlist\"/>\n");
                writer.write("  </tractor>\n");
                writer.write("</mlt>\n");
            }
        } catch (IOException e) {
            LOG.log(Level.ERROR, "Failed to write MLT XML", e);
            throw new IllegalStateException("MLT rendering failed", e);
        }

        return new RenderResult(outputPath, timelineMs, sorted.size());
    }

    private long toFrames(long durationMs, double frameRate) {
        return Math.max(1L, Math.round(durationMs / 1000.0 * frameRate));
    }

    private String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
