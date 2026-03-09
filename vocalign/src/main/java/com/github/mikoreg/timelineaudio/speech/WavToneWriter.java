package com.github.mikoreg.timelineaudio.speech;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WavToneWriter {
    private static final int SAMPLE_RATE = 24000;
    private static final short CHANNELS = 1;
    private static final short BITS_PER_SAMPLE = 16;

    private WavToneWriter() {
    }

    public static void writeTone(Path outputPath, long durationMs, double frequencyHz, double amplitude) throws IOException {
        long totalSamples = Math.max(1, Math.round((durationMs / 1000.0) * SAMPLE_RATE));
        int byteRate = SAMPLE_RATE * CHANNELS * BITS_PER_SAMPLE / 8;
        int blockAlign = CHANNELS * BITS_PER_SAMPLE / 8;
        long dataSize = totalSamples * blockAlign;
        long chunkSize = 36 + dataSize;

        Files.createDirectories(outputPath.getParent());
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(outputPath))) {
            out.write("RIFF".getBytes());
            writeIntLE(out, (int) chunkSize);
            out.write("WAVE".getBytes());

            out.write("fmt ".getBytes());
            writeIntLE(out, 16);
            writeShortLE(out, (short) 1);
            writeShortLE(out, CHANNELS);
            writeIntLE(out, SAMPLE_RATE);
            writeIntLE(out, byteRate);
            writeShortLE(out, (short) blockAlign);
            writeShortLE(out, BITS_PER_SAMPLE);

            out.write("data".getBytes());
            writeIntLE(out, (int) dataSize);

            double angular = 2.0 * Math.PI * frequencyHz / SAMPLE_RATE;
            double scaled = Math.min(Math.max(amplitude, 0.0), 1.0) * Short.MAX_VALUE;
            byte[] frame = new byte[2];
            for (long i = 0; i < totalSamples; i++) {
                short sample = (short) Math.round(Math.sin(angular * i) * scaled);
                frame[0] = (byte) (sample & 0xFF);
                frame[1] = (byte) ((sample >> 8) & 0xFF);
                out.write(frame);
            }
        }
    }

    private static void writeIntLE(OutputStream out, int value) throws IOException {
        byte[] bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
        out.write(bytes);
    }

    private static void writeShortLE(OutputStream out, short value) throws IOException {
        byte[] bytes = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array();
        out.write(bytes);
    }
}
