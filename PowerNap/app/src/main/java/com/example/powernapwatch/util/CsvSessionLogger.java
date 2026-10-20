package com.example.powernapwatch.util;

import android.content.Context;
import android.os.Build;

import com.example.powernapwatch.core.DetectorSnapshot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Raw diagnostic logger. Besides the sensor timestamp it records when the app
 * actually received each sample. delivery_delay_ms makes batching, stalls and
 * timestamp anomalies visible during post-session analysis.
 */
public final class CsvSessionLogger implements AutoCloseable {
    private static final int AUTO_FLUSH_LINES = 100;

    private final BufferedWriter writer;
    private final File file;
    private long sequence;
    private int pendingLines;

    public CsvSessionLogger(Context context) throws IOException {
        File dir = new File(context.getFilesDir(), "sessions");
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("Cannot create sessions directory");
        String name = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(new Date()) + ".csv";
        file = new File(dir, name);
        writer = new BufferedWriter(new FileWriter(file, false), 64 * 1024);
        writer.write("seq,type,sensor_timestamp_ms,received_timestamp_ms,delivery_delay_ms,x,y,z,hr,ibi,status,state,confidence,motion_rms,rmssd,reason\n");
        writer.flush();
    }

    public String path() { return file.getAbsolutePath(); }
    public String fileName() { return file.getName(); }

    public synchronized void writeSessionHeader(String selectedEngine, String effectiveProvider) {
        event(System.currentTimeMillis(), "SESSION_START selectedEngine=" + selectedEngine
                + "; provider=" + effectiveProvider
                + "; device=" + Build.MANUFACTURER + " " + Build.MODEL
                + "; android=" + Build.VERSION.RELEASE
                + "; sdk=" + Build.VERSION.SDK_INT);
    }

    public synchronized void accel(long sensorT, double x, double y, double z) {
        long received = System.currentTimeMillis();
        line("ACC", sensorT, received, x, y, z, null, null, null, null, null, null, null, null);
    }

    public synchronized void heart(long sensorT, double hr, int status) {
        long received = System.currentTimeMillis();
        line("HR", sensorT, received, null, null, null, hr, null, status, null, null, null, null, null);
    }

    public synchronized void ibi(long sensorT, double ibi, int status) {
        long received = System.currentTimeMillis();
        line("IBI", sensorT, received, null, null, null, null, ibi, status, null, null, null, null, null);
    }

    public synchronized void snapshot(DetectorSnapshot s) {
        long received = System.currentTimeMillis();
        line("SNAP", s.timestampMs(), received, null, null, null, s.currentHeartRate(), null, null,
                s.state().name(), s.confidence(), s.movementRms(), s.rmssdMs(), s.reason());
    }

    public synchronized void event(long sensorT, String text) {
        long received = System.currentTimeMillis();
        line("EVENT", sensorT, received, null, null, null, null, null, null, null, null, null, null, text);
    }

    /** Diagnostic metadata. Unlike EVENT it does not force an immediate fsync/flush. */
    public synchronized void diagnostic(String text) {
        long now = System.currentTimeMillis();
        line("DIAG", now, now, null, null, null, null, null, null, null, null, null, null, text);
    }

    private void line(String type, long sensorT, long receivedT,
                      Double x, Double y, Double z, Double hr, Double ibi,
                      Integer status, String state, Double confidence, Double motion, Double rmssd,
                      String reason) {
        try {
            writer.write(Long.toString(++sequence)); writer.write(',');
            writer.write(type); writer.write(',');
            writer.write(Long.toString(sensorT)); writer.write(',');
            writer.write(Long.toString(receivedT)); writer.write(',');
            writer.write(Long.toString(receivedT - sensorT)); writer.write(',');
            writer.write(num(x)); writer.write(','); writer.write(num(y)); writer.write(','); writer.write(num(z)); writer.write(',');
            writer.write(num(hr)); writer.write(','); writer.write(num(ibi)); writer.write(',');
            writer.write(status == null ? "" : status.toString()); writer.write(',');
            writer.write(csv(state)); writer.write(','); writer.write(num(confidence)); writer.write(',');
            writer.write(num(motion)); writer.write(','); writer.write(num(rmssd)); writer.write(',');
            writer.write(csv(reason)); writer.write('\n');
            pendingLines++;
            if (pendingLines >= AUTO_FLUSH_LINES || "EVENT".equals(type)) {
                writer.flush();
                pendingLines = 0;
            }
        } catch (IOException ignored) { }
    }

    private static String num(Double d) {
        if (d == null || Double.isNaN(d)) return "";
        return String.format(Locale.ROOT, "%.6f", d);
    }

    private static String csv(String s) {
        if (s == null) return "";
        return '"' + s.replace("\"", "\"\"") + '"';
    }

    /**
     * Append feedback to the exact CSV of the completed session.
     * Sensor logging can already be closed at this point (including after an alarm).
     */
    public static synchronized void appendFeedback(Context context, String sessionFile,
                                                    boolean slept, String finalState) throws IOException {
        File dir = new File(context.getFilesDir(), "sessions").getCanonicalFile();
        if (sessionFile == null || sessionFile.isBlank()) {
            throw new IOException("No session file available for feedback");
        }
        File target = new File(sessionFile).getCanonicalFile();
        if (!dir.equals(target.getParentFile()) || !target.isFile()) {
            throw new IOException("Session CSV missing or outside sessions directory: " + target);
        }
        long lastSeq = findLastSequence(target);
        try (BufferedWriter append = new BufferedWriter(new FileWriter(target, true))) {
            append.write(FeedbackCsvRow.format(lastSeq + 1, System.currentTimeMillis(), slept, finalState));
            append.flush();
        }
    }

    /** Read only the last line, not the full (possibly very large) raw-sensor file. */
    private static long findLastSequence(File file) throws IOException {
        try (RandomAccessFile in = new RandomAccessFile(file, "r")) {
            long pos = in.length() - 1;
            while (pos >= 0) {
                in.seek(pos);
                int b = in.read();
                if (b != '\n' && b != '\r') break;
                pos--;
            }
            while (pos >= 0) {
                in.seek(pos);
                if (in.read() == '\n') break;
                pos--;
            }
            in.seek(pos + 1);
            String lastLine = in.readLine();
            if (lastLine == null) return 0;
            int separator = lastLine.indexOf(',');
            if (separator <= 0) return 0; // empty log / header only
            try {
                return Long.parseLong(lastLine.substring(0, separator));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    @Override
    public synchronized void close() {
        try { writer.flush(); } catch (IOException ignored) { }
        try { writer.close(); } catch (IOException ignored) { }
    }
}
