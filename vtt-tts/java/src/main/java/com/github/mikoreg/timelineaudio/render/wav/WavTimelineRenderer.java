package com.github.mikoreg.timelineaudio.render.wav;

import com.github.mikoreg.timelineaudio.domain.DebugLogger;
import com.github.mikoreg.timelineaudio.domain.RenderJob;
import com.github.mikoreg.timelineaudio.domain.RenderResult;
import com.github.mikoreg.timelineaudio.domain.RenderSegment;
import com.github.mikoreg.timelineaudio.render.AudioRenderer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

public class WavTimelineRenderer implements AudioRenderer {
    private static final int SAMPLE_RATE = 24000;
    private static final short CHANNELS = 1;
    private static final short BITS_PER_SAMPLE = 16;
    private static final int HEADER_SIZE = 44;

    private final DebugLogger logger;

    public WavTimelineRenderer(DebugLogger logger) {
        this.logger = logger;
    }

    @Override
    public RenderResult render(RenderJob job) {
        Path output = job.outputPath();
        long timelineMs = job.timelineDurationMs();
        logger.info("Rendering WAV timeline to " + output);

        long totalSamples = Math.max(1, Math.round((timelineMs / 1000.0) * SAMPLE_RATE));
        long dataSize = totalSamples * (BITS_PER_SAMPLE / 8);

        try {
            Files.createDirectories(output.getParent());
            try (RandomAccessFile raf = new RandomAccessFile(output.toFile(), "rw")) {
                raf.setLength(HEADER_SIZE + dataSize);
                writeHeader(raf, dataSize);

                for (RenderSegment segment : job.segments()) {
                    writeToneSegment(raf, segment);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to render WAV timeline", e);
            throw new IllegalStateException("WAV rendering failed", e);
        }

        return new RenderResult(output, timelineMs, job.segments().size());
    }

    private void writeToneSegment(RandomAccessFile raf, RenderSegment segment) throws IOException {
        long startSample = Math.max(0, Math.round((segment.startMs() / 1000.0) * SAMPLE_RATE));
        long totalSamples = Math.max(1, Math.round((segment.durationMs() / 1000.0) * SAMPLE_RATE));
        long dataOffset = HEADER_SIZE + startSample * 2;

        double frequency = 440.0 * Math.max(0.5, Math.min(segment.speedFactor(), 2.0));
        double angular = 2.0 * Math.PI * frequency / SAMPLE_RATE;
        double amplitude = 0.2 * Short.MAX_VALUE;

        logger.debug("Writing tone segment " + segment.id() + " start=" + segment.startMs()
                + " ms duration=" + segment.durationMs() + " ms freq=" + frequency + " Hz");

        raf.seek(dataOffset);
        byte[] frame = new byte[2];
        for (long i = 0; i < totalSamples; i++) {
            short sample = (short) Math.round(Math.sin(angular * i) * amplitude);
            frame[0] = (byte) (sample & 0xFF);
            frame[1] = (byte) ((sample >> 8) & 0xFF);
            raf.write(frame);
        }
    }

    private void writeHeader(RandomAccessFile raf, long dataSize) throws IOException {
        int byteRate = SAMPLE_RATE * CHANNELS * BITS_PER_SAMPLE / 8;
        int blockAlign = CHANNELS * BITS_PER_SAMPLE / 8;
        long chunkSize = 36 + dataSize;

        raf.seek(0);
        raf.writeBytes("RIFF");
        writeIntLE(raf, (int) chunkSize);
        raf.writeBytes("WAVE");

        raf.writeBytes("fmt ");
        writeIntLE(raf, 16);
        writeShortLE(raf, (short) 1);
        writeShortLE(raf, CHANNELS);
        writeIntLE(raf, SAMPLE_RATE);
        writeIntLE(raf, byteRate);
        writeShortLE(raf, (short) blockAlign);
        writeShortLE(raf, BITS_PER_SAMPLE);

        raf.writeBytes("data");
        writeIntLE(raf, (int) dataSize);
    }

    private void writeIntLE(RandomAccessFile raf, int value) throws IOException {
        byte[] bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
        raf.write(bytes);
    }

    private void writeShortLE(RandomAccessFile raf, short value) throws IOException {
        byte[] bytes = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array();
        raf.write(bytes);
    }
}
