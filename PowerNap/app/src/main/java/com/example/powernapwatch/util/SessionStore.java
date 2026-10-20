package com.example.powernapwatch.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.powernapwatch.core.DetectorSnapshot;
import com.example.powernapwatch.core.NapState;

public final class SessionStore {
    private static final String PREFS = "nap_session";
    private final SharedPreferences p;

    public SessionStore(Context context) {
        p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void reset(String selectedEngine, String provider, boolean loggingEnabled, String file) {
        p.edit().clear()
                .putString("state", NapState.BASELINE.name())
                .putString("selectedEngine", selectedEngine)
                .putString("provider", provider)
                .putBoolean("loggingEnabled", loggingEnabled)
                .putString("file", file == null ? "" : file)
                .putLong("sessionStartedAt", System.currentTimeMillis())
                .putBoolean("feedbackPending", false)
                .putString("feedback", "")
                .apply();
    }

    public void snapshot(DetectorSnapshot s) {
        p.edit()
                .putString("state", s.state().name())
                .putFloat("confidence", (float) s.confidence())
                .putFloat("hr", (float) s.currentHeartRate())
                .putFloat("baselineHr", (float) s.baselineHeartRate())
                .putFloat("motion", (float) s.movementRms())
                .putFloat("rmssd", (float) s.rmssdMs())
                .putString("message", s.reason() == null ? "" : s.reason())
                .apply();
    }

    public void state(NapState state, String message) {
        p.edit().putString("state", state.name()).putString("message", message).apply();
    }

    /** A completed/cancelled session keeps its feedback question across app restarts. */
    public void finish(NapState state, String message) {
        p.edit().putString("state", state.name())
                .putString("message", message)
                .putBoolean("feedbackPending", true)
                .commit();
    }

    public void feedbackSaved(boolean sleepOccurred) {
        p.edit().putBoolean("feedbackPending", false)
                .putString("feedback", sleepOccurred ? "YES" : "NO")
                .putLong("feedbackAt", System.currentTimeMillis())
                .commit();
    }

    public void logFile(String file) {
        p.edit().putString("file", file == null ? "" : file).commit();
    }

    public void providerMessage(String provider, String message) {
        p.edit().putString("provider", provider).putString("message", message).apply();
    }

    public void napScheduled(long onsetAtMs, long alarmAtMs, double confidence) {
        p.edit().putString("state", NapState.NAPPING.name())
                .putLong("onsetAt", onsetAtMs)
                .putLong("alarmAt", alarmAtMs)
                .putFloat("confidence", (float) confidence)
                .putString("message", "Sleep onset detected")
                .apply();
    }

    public Snapshot read() {
        NapState state;
        try { state = NapState.valueOf(p.getString("state", NapState.IDLE.name())); }
        catch (Exception e) { state = NapState.IDLE; }
        return new Snapshot(
                state,
                p.getString("selectedEngine", "--"),
                p.getString("provider", "--"),
                p.getBoolean("loggingEnabled", false),
                p.getFloat("hr", Float.NaN),
                p.getFloat("baselineHr", Float.NaN),
                p.getFloat("motion", Float.NaN),
                p.getFloat("rmssd", Float.NaN),
                p.getFloat("confidence", 0f),
                p.getString("message", ""),
                p.getLong("onsetAt", 0),
                p.getLong("alarmAt", 0),
                p.getString("file", ""),
                p.getBoolean("feedbackPending", false),
                p.getString("feedback", ""),
                p.getLong("sessionStartedAt", 0)
        );
    }

    public record Snapshot(NapState state, String selectedEngine, String provider, boolean loggingEnabled,
                           float heartRate, float baselineHeartRate, float movement, float rmssd,
                           float confidence, String message, long onsetAtMs, long alarmAtMs, String file,
                           boolean feedbackPending, String feedback, long sessionStartedAtMs) {}
}
