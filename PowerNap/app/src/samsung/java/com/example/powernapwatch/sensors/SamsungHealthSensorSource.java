package com.example.powernapwatch.sensors;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.example.powernapwatch.util.TimestampNormalizer;
import com.samsung.android.service.health.tracking.ConnectionListener;
import com.samsung.android.service.health.tracking.HealthTracker;
import com.samsung.android.service.health.tracking.HealthTrackerException;
import com.samsung.android.service.health.tracking.HealthTrackingService;
import com.samsung.android.service.health.tracking.data.DataPoint;
import com.samsung.android.service.health.tracking.data.HealthTrackerType;
import com.samsung.android.service.health.tracking.data.ValueKey;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Galaxy Watch4+ implementation using Samsung Health Sensor SDK.
 *
 * The Samsung SDK batches continuous sensor data while the display is off.
 * Power Nap therefore requests a flush every few seconds and records both the
 * request/result and flush-completion latency for diagnostics.
 */
public final class SamsungHealthSensorSource implements SleepSignalSource {
    private static final String TAG = "PowerNap";
    private static final double ACC_SCALE = 9.81 / (16383.75 / 4.0);
    private static final long FLUSH_PERIOD_SECONDS = 5L;

    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final TimestampNormalizer timestampNormalizer = new TimestampNormalizer();
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final StandardWearSensorSource fallback;
    private final boolean allowFallback;

    private final AtomicBoolean hrFlushPending = new AtomicBoolean(false);
    private final AtomicBoolean accFlushPending = new AtomicBoolean(false);
    private final AtomicLong hrFlushRequestedElapsed = new AtomicLong(0);
    private final AtomicLong accFlushRequestedElapsed = new AtomicLong(0);

    private Listener listener;
    private HealthTrackingService service;
    private HealthTracker hrTracker;
    private HealthTracker accTracker;
    private ScheduledExecutorService flusher;
    private boolean fallbackActive;
    private boolean connected;
    private Runnable connectionTimeoutRunnable;
    private Boolean lastOnBody;

    public SamsungHealthSensorSource(Context context, boolean allowFallback) {
        this.context = context.getApplicationContext();
        this.allowFallback = allowFallback;
        this.fallback = new StandardWearSensorSource(this.context);
    }

    @Override
    public String name() {
        return fallbackActive ? "Wear OS fallback" : "Samsung Health Sensor SDK";
    }

    @Override
    public void start(Listener listener) {
        this.listener = listener;
        stopped.set(false);
        fallbackActive = false;
        connected = false;
        lastOnBody = null;
        hrFlushPending.set(false);
        accFlushPending.set(false);
        Log.i(TAG, "SamsungHealthSensorSource start: connecting...");
        listener.onSourceStatus("Connecting to Samsung Health Sensor Service...");

        service = new HealthTrackingService(new ConnectionListener() {
            @Override
            public void onConnectionSuccess() {
                connected = true;
                if (connectionTimeoutRunnable != null) mainHandler.removeCallbacks(connectionTimeoutRunnable);
                Log.i(TAG, "Samsung Health Tracking Service onConnectionSuccess");
                if (stopped.get()) return;
                try {
                    List<HealthTrackerType> supported = service.getTrackingCapability().getSupportHealthTrackerTypes();
                    Log.i(TAG, "Supported trackers: " + supported);
                    if (!supported.contains(HealthTrackerType.HEART_RATE_CONTINUOUS)
                            || !supported.contains(HealthTrackerType.ACCELEROMETER_CONTINUOUS)) {
                        switchToFallback("Required Samsung continuous trackers unavailable");
                        return;
                    }
                    hrTracker = service.getHealthTracker(HealthTrackerType.HEART_RATE_CONTINUOUS);
                    accTracker = service.getHealthTracker(HealthTrackerType.ACCELEROMETER_CONTINUOUS);
                    hrTracker.setEventListener(hrListener);
                    accTracker.setEventListener(accListener);
                    startFlushLoop();
                    Log.i(TAG, "Samsung HR+IBI and ACC trackers active");
                    listener.onSourceStatus("Samsung HR+IBI and ACC active; flush=" + FLUSH_PERIOD_SECONDS + "s");
                } catch (Throwable t) {
                    Log.e(TAG, "Samsung tracker startup failed", t);
                    switchToFallback("Samsung tracker startup failed: " + t.getMessage());
                }
            }

            @Override
            public void onConnectionEnded() {
                connected = false;
                if (connectionTimeoutRunnable != null) mainHandler.removeCallbacks(connectionTimeoutRunnable);
                Log.w(TAG, "Samsung Health Sensor Service connection ended");
                if (!stopped.get()) listener.onSourceStatus("Samsung Health Sensor Service disconnected");
            }

            @Override
            public void onConnectionFailed(HealthTrackerException e) {
                connected = false;
                if (connectionTimeoutRunnable != null) mainHandler.removeCallbacks(connectionTimeoutRunnable);
                Log.e(TAG, "Samsung Health Sensor Service connection failed: " + (e == null ? "null" : e.getMessage()));
                switchToFallback("Samsung service connection failed: " + (e == null ? "null" : e.getMessage()));
            }
        }, context);

        connectionTimeoutRunnable = () -> {
            if (!connected && !fallbackActive && !stopped.get()) {
                Log.w(TAG, "Samsung Health Tracking Service connection timed out after 5s");
                switchToFallback("Samsung service timeout (Developer mode off or incompatible SDK)");
            }
        };
        mainHandler.postDelayed(connectionTimeoutRunnable, 5_000L);

        try {
            service.connectService();
        } catch (Throwable t) {
            Log.e(TAG, "service.connectService() failed", t);
            if (connectionTimeoutRunnable != null) mainHandler.removeCallbacks(connectionTimeoutRunnable);
            switchToFallback("Samsung service unavailable: " + t.getMessage());
        }
    }

    private final HealthTracker.TrackerEventListener hrListener = new HealthTracker.TrackerEventListener() {
        @Override
        public void onDataReceived(List<DataPoint> data) {
            Listener l = listener;
            if (l == null || data == null || data.isEmpty()) return;

            long firstT = Long.MAX_VALUE;
            long lastT = Long.MIN_VALUE;
            int totalIbi = 0;
            int maxIbiList = 0;

            for (DataPoint dp : data) {
                long t = timestampNormalizer.normalize(dp.getTimestamp());
                firstT = Math.min(firstT, t);
                lastT = Math.max(lastT, t);

                Integer hr = dp.getValue(ValueKey.HeartRateSet.HEART_RATE);
                Integer hrStatus = dp.getValue(ValueKey.HeartRateSet.HEART_RATE_STATUS);
                if (hrStatus != null) {
                    Boolean currentOnBody = hrStatus == 1 ? Boolean.TRUE : (hrStatus == -3 ? Boolean.FALSE : null);
                    if (currentOnBody != null && !currentOnBody.equals(lastOnBody)) {
                        lastOnBody = currentOnBody;
                        l.onBodyState(t, currentOnBody);
                    }
                }

                if (hr != null && hrStatus != null && hrStatus == 1) {
                    l.onHeartRate(t, hr, hrStatus);
                }

                List<Integer> ibiValues = dp.getValue(ValueKey.HeartRateSet.IBI_LIST);
                List<Integer> ibiStatuses = dp.getValue(ValueKey.HeartRateSet.IBI_STATUS_LIST);
                if (ibiValues != null && ibiStatuses != null) {
                    int n = Math.min(ibiValues.size(), ibiStatuses.size());
                    totalIbi += n;
                    maxIbiList = Math.max(maxIbiList, n);
                    emitIbiWithEstimatedTimestamps(l, t, ibiValues, ibiStatuses, n);
                }
            }

            l.onSourceDiagnostic("HR_BATCH points=" + data.size()
                    + "; first=" + firstT + "; last=" + lastT
                    + "; ibi=" + totalIbi + "; maxIbiList=" + maxIbiList);
        }

        @Override
        public void onFlushCompleted() {
            long requested = hrFlushRequestedElapsed.getAndSet(0);
            hrFlushPending.set(false);
            long latency = requested == 0 ? -1 : SystemClock.elapsedRealtime() - requested;
            Listener l = listener;
            if (l != null) l.onSourceDiagnostic("FLUSH_COMPLETE HR latency_ms=" + latency);
        }

        @Override
        public void onError(HealthTracker.TrackerError error) {
            if (error == HealthTracker.TrackerError.SDK_POLICY_ERROR) {
                switchToFallback("Samsung SDK policy denied by Health Sensor Service");
            } else {
                switchToFallback("Samsung HR tracker error: " + error);
            }
        }
    };

    private final HealthTracker.TrackerEventListener accListener = new HealthTracker.TrackerEventListener() {
        @Override
        public void onDataReceived(List<DataPoint> data) {
            Listener l = listener;
            if (l == null || data == null || data.isEmpty()) return;
            long firstT = Long.MAX_VALUE;
            long lastT = Long.MIN_VALUE;

            for (DataPoint dp : data) {
                long t = timestampNormalizer.normalize(dp.getTimestamp());
                firstT = Math.min(firstT, t);
                lastT = Math.max(lastT, t);
                Integer x = dp.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_X);
                Integer y = dp.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Y);
                Integer z = dp.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Z);
                if (x != null && y != null && z != null) {
                    l.onAcceleration(t, x * ACC_SCALE, y * ACC_SCALE, z * ACC_SCALE);
                }
            }

            l.onSourceDiagnostic("ACC_BATCH points=" + data.size()
                    + "; first=" + firstT + "; last=" + lastT);
        }

        @Override
        public void onFlushCompleted() {
            long requested = accFlushRequestedElapsed.getAndSet(0);
            accFlushPending.set(false);
            long latency = requested == 0 ? -1 : SystemClock.elapsedRealtime() - requested;
            Listener l = listener;
            if (l != null) l.onSourceDiagnostic("FLUSH_COMPLETE ACC latency_ms=" + latency);
        }

        @Override
        public void onError(HealthTracker.TrackerError error) {
            if (error == HealthTracker.TrackerError.SDK_POLICY_ERROR) {
                switchToFallback("Samsung SDK policy denied by Health Sensor Service");
            } else {
                switchToFallback("Samsung ACC tracker error: " + error);
            }
        }
    };

    /**
     * Samsung stores all IBI values accumulated for a batched interval in the
     * first HR DataPoint. They therefore cannot all be stamped with that single
     * DataPoint timestamp. Reconstruct approximate beat times backwards from
     * the DataPoint timestamp so HRV windows are at least temporally coherent.
     */
    private static void emitIbiWithEstimatedTimestamps(Listener l, long endTimestampMs,
                                                        List<Integer> values,
                                                        List<Integer> statuses,
                                                        int n) {
        if (n <= 0) return;
        long[] times = new long[n];
        long cursor = endTimestampMs;
        for (int i = n - 1; i >= 0; i--) {
            times[i] = cursor;
            Integer value = values.get(i);
            if (value != null && value > 0) cursor -= value;
        }
        for (int i = 0; i < n; i++) {
            Integer value = values.get(i);
            Integer status = statuses.get(i);
            if (value != null && status != null && status == 0 && value > 0) {
                l.onIbi(times[i], value, status);
            }
        }
    }

    private synchronized void startFlushLoop() {
        if (flusher != null) return;
        flusher = Executors.newSingleThreadScheduledExecutor();
        flusher.scheduleWithFixedDelay(this::requestFlushes,
                FLUSH_PERIOD_SECONDS, FLUSH_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    private void requestFlushes() {
        requestFlush("HR", hrTracker, hrFlushPending, hrFlushRequestedElapsed);
        requestFlush("ACC", accTracker, accFlushPending, accFlushRequestedElapsed);
    }

    private void requestFlush(String label, HealthTracker tracker,
                              AtomicBoolean pending, AtomicLong requestedAt) {
        if (tracker == null || stopped.get()) return;
        Listener l = listener;
        long nowElapsed = SystemClock.elapsedRealtime();
        if (!pending.compareAndSet(false, true)) {
            long since = requestedAt.get();
            if (l != null) l.onSourceDiagnostic("FLUSH_SKIP " + label
                    + " pending_ms=" + (since == 0 ? -1 : nowElapsed - since));
            return;
        }
        requestedAt.set(nowElapsed);
        try {
            boolean accepted = tracker.flush();
            if (l != null) l.onSourceDiagnostic("FLUSH_REQUEST " + label + " accepted=" + accepted);
            if (!accepted) {
                pending.set(false);
                requestedAt.set(0);
            }
        } catch (Throwable t) {
            pending.set(false);
            requestedAt.set(0);
            if (l != null) l.onSourceDiagnostic("FLUSH_ERROR " + label + " " + t.getClass().getSimpleName()
                    + ": " + t.getMessage());
        }
    }

    private synchronized void switchToFallback(String reason) {
        if (stopped.get() || fallbackActive) return;
        Log.w(TAG, "SamsungHealthSensorSource failure: " + reason + ", allowFallback=" + allowFallback);
        if (!allowFallback) {
            stopSamsungOnly();
            Listener l = listener;
            if (l != null) l.onSourceError(reason, null);
            return;
        }
        fallbackActive = true;
        stopSamsungOnly();
        Listener l = listener;
        if (l != null) {
            l.onSourceStatus(reason + " -> using standard Wear sensors");
            fallback.start(l);
        }
    }

    private synchronized void stopSamsungOnly() {
        if (connectionTimeoutRunnable != null) mainHandler.removeCallbacks(connectionTimeoutRunnable);
        if (flusher != null) {
            flusher.shutdownNow();
            flusher = null;
        }
        hrFlushPending.set(false);
        accFlushPending.set(false);
        hrFlushRequestedElapsed.set(0);
        accFlushRequestedElapsed.set(0);
        try { if (hrTracker != null) hrTracker.unsetEventListener(); } catch (Throwable ignored) { }
        try { if (accTracker != null) accTracker.unsetEventListener(); } catch (Throwable ignored) { }
        hrTracker = null;
        accTracker = null;
        try { if (service != null) service.disconnectService(); } catch (Throwable ignored) { }
        service = null;
    }

    @Override
    public synchronized void stop() {
        stopped.set(true);
        stopSamsungOnly();
        fallback.stop();
        listener = null;
    }
}
