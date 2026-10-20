package com.example.powernapwatch.sensors;

public interface SleepSignalSource {

    interface Listener {
        void onAcceleration(long timestampMs, double x, double y, double z);
        void onHeartRate(long timestampMs, double bpm, int status);
        void onIbi(long timestampMs, double ibiMs, int status);
        void onBodyState(long timestampMs, boolean onBody);
        void onSourceStatus(String message);
        void onSourceError(String message, Throwable error);

        /** High-volume diagnostics that should be recorded, but not shown as UI status. */
        default void onSourceDiagnostic(String message) { }
    }

    String name();
    void start(Listener listener);
    void stop();
}
