package com.example.powernapwatch.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.example.powernapwatch.R;
import com.example.powernapwatch.core.DetectorSnapshot;
import com.example.powernapwatch.core.NapState;
import com.example.powernapwatch.core.SleepOnsetDetector;
import com.example.powernapwatch.sensors.SensorEngine;
import com.example.powernapwatch.sensors.SleepSignalSource;
import com.example.powernapwatch.sensors.SleepSignalSourceFactory;
import com.example.powernapwatch.util.CsvSessionLogger;
import com.example.powernapwatch.util.NapAlarmScheduler;
import com.example.powernapwatch.util.SessionStore;
import com.example.powernapwatch.util.UserPreferences;

import java.io.IOException;

public final class NapMonitoringService extends Service {
    public static final String ACTION_START = "com.example.powernapwatch.START";
    public static final String ACTION_STOP = "com.example.powernapwatch.STOP";
    public static final String EXTRA_ENGINE = "sensor_engine";
    public static final String EXTRA_LOGGING = "record_sensor_data";

    private static final String TAG = "PowerNap";
    private static final String CHANNEL = "power_nap_monitor";
    private static final int NOTIFICATION_ID = 1101;
    private static final long NAP_DURATION_MS = 10 * 60_000L;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SleepSignalSource source;
    private SleepOnsetDetector detector;
    private CsvSessionLogger logger;
    private SessionStore store;
    private boolean active;
    private boolean recordRawSensors;
    private Boolean lastOnBody;
    private NapState lastLoggedState;

    @Override
    public void onCreate() {
        super.onCreate();
        store = new SessionStore(this);
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        if (ACTION_STOP.equals(action)) {
            cancelAndStop("Cancelled by user");
            return START_NOT_STICKY;
        }
        if (!active) startMonitoring(intent);
        return START_NOT_STICKY;
    }

    private void startMonitoring(Intent intent) {
        Notification notification = monitorNotification("Collecting baseline...");
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        if (!NapAlarmScheduler.canScheduleExact(this)) {
            store.state(NapState.ERROR, "Exact alarm permission is required");
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            return;
        }

        UserPreferences preferences = new UserPreferences(this);
        SensorEngine selectedEngine = SensorEngine.fromName(
                intent == null ? null : intent.getStringExtra(EXTRA_ENGINE));
        if (intent == null || !intent.hasExtra(EXTRA_ENGINE)) selectedEngine = preferences.engine();
        boolean loggingEnabled = intent != null && intent.hasExtra(EXTRA_LOGGING)
                ? intent.getBooleanExtra(EXTRA_LOGGING, true)
                : preferences.loggingEnabled();

        preferences.engine(selectedEngine);
        preferences.loggingEnabled(loggingEnabled);
        active = true;
        recordRawSensors = loggingEnabled;
        lastOnBody = null;
        lastLoggedState = null;

        String loggingProblem = null;
        try {
            logger = new CsvSessionLogger(this);
            Log.i(TAG, "Session activity CSV: " + logger.path());
        } catch (IOException e) {
            loggingProblem = "CSV logger unavailable: " + e.getMessage();
            Log.e(TAG, loggingProblem, e);
        }

        // Keep the activity log (and later FEEDBACK) even if raw sensor recording is disabled.
        store.reset(selectedEngine.label(), selectedEngine.label(), loggingEnabled,
                logger == null ? "" : logger.path());
        if (logger != null) {
            logger.writeSessionHeader(selectedEngine.label(), selectedEngine.label());
            logger.event(System.currentTimeMillis(), "RAW_SENSOR_RECORDING=" + loggingEnabled);
        }

        try {
            source = SleepSignalSourceFactory.create(this, selectedEngine);
        } catch (Throwable t) {
            failAndStop("Cannot create sensor engine " + selectedEngine.label() + ": " + t.getMessage(), t);
            return;
        }

        store.providerMessage(source.name(), loggingProblem == null ? "Monitoring started" : loggingProblem);
        if (logger != null) logger.event(System.currentTimeMillis(), "SOURCE_SELECTED " + source.name());
        Log.i(TAG, "Selected engine=" + selectedEngine + ", source=" + source.name()
                + ", logging=" + loggingEnabled);

        SleepOnsetDetector.Config cfg = new SleepOnsetDetector.Config();
        detector = new SleepOnsetDetector(cfg, new SleepOnsetDetector.Listener() {
            @Override
            public void onSnapshot(DetectorSnapshot snapshot) {
                store.snapshot(snapshot);
                if (logger != null) {
                    if (recordRawSensors) logger.snapshot(snapshot);
                    else if (lastLoggedState != snapshot.state()) {
                        logger.event(snapshot.timestampMs(), "STATE_CHANGE " + snapshot.state()
                                + "; confidence=" + snapshot.confidence());
                        lastLoggedState = snapshot.state();
                    }
                }
                updateNotification(snapshot.state() + "  " + Math.round(snapshot.confidence() * 100) + "%");
            }

            @Override
            public void onSleepConfirmed(long estimatedOnsetMs, double confidence) {
                handleSleepConfirmed(estimatedOnsetMs, confidence);
            }
        });
        detector.start(System.currentTimeMillis());

        try {
            source.start(new SleepSignalSource.Listener() {
            @Override
            public void onAcceleration(long timestampMs, double x, double y, double z) {
                if (!active) return;
                if (recordRawSensors && logger != null) logger.accel(timestampMs, x, y, z);
                detector.onAcceleration(timestampMs, x, y, z);
            }

            @Override
            public void onHeartRate(long timestampMs, double bpm, int status) {
                if (!active) return;
                if (recordRawSensors && logger != null) logger.heart(timestampMs, bpm, status);
                detector.onHeartRate(timestampMs, bpm);
            }

            @Override
            public void onIbi(long timestampMs, double ibiMs, int status) {
                if (!active) return;
                if (recordRawSensors && logger != null) logger.ibi(timestampMs, ibiMs, status);
                if (status == 0) detector.onIbi(timestampMs, ibiMs);
            }

            @Override
            public void onBodyState(long timestampMs, boolean onBody) {
                if (!active) return;
                if (logger != null && (lastOnBody == null || lastOnBody != onBody)) {
                    logger.event(timestampMs, "onBody=" + onBody);
                    lastOnBody = onBody;
                }
                detector.onBodyState(onBody, timestampMs);
            }

            @Override
            public void onSourceStatus(String statusMessage) {
                String effective = source == null ? "--" : source.name();
                Log.i(TAG, "Source status [" + effective + "]: " + statusMessage);
                store.providerMessage(effective, statusMessage);
                if (logger != null) logger.event(System.currentTimeMillis(),
                        "SOURCE_STATUS provider=" + effective + "; " + statusMessage);
            }

            @Override
            public void onSourceDiagnostic(String diagnosticMessage) {
                Log.d(TAG, diagnosticMessage);
                if (recordRawSensors && logger != null) logger.diagnostic(diagnosticMessage);
            }

            @Override
            public void onSourceError(String errorMessage, Throwable error) {
                Log.e(TAG, "Sensor source error: " + errorMessage, error);
                if (logger != null) logger.event(System.currentTimeMillis(), "SOURCE_ERROR " + errorMessage);
                mainHandler.post(() -> failAndStop(errorMessage, error));
            }
            });
        } catch (Throwable t) {
            failAndStop("Cannot start sensor engine: " + t.getMessage(), t);
        }
    }

    private synchronized void handleSleepConfirmed(long estimatedOnsetMs, double confidence) {
        if (!active) return;
        long now = System.currentTimeMillis();
        long alarmAt = Math.max(now + 2_000, estimatedOnsetMs + NAP_DURATION_MS);
        if (logger != null) logger.event(now, "SLEEP_CONFIRMED sleepOnset=" + estimatedOnsetMs
                + "; alarmAt=" + alarmAt + "; confidence=" + confidence);
        NapAlarmScheduler.schedule(this, alarmAt);
        store.napScheduled(estimatedOnsetMs, alarmAt, confidence);
        updateNotification("Sleep detected. Alarm scheduled.");

        if (source != null) source.stop();
        active = false;
        closeLogger();
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    private synchronized void failAndStop(String reason, Throwable error) {
        if (!active) return;
        Log.e(TAG, "Stopping after error: " + reason, error);
        store.finish(NapState.ERROR, reason);
        if (logger != null) logger.event(System.currentTimeMillis(), "FATAL " + reason);
        if (source != null) {
            try { source.stop(); } catch (Throwable ignored) { }
        }
        active = false;
        closeLogger();
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    private synchronized void cancelAndStop(String reason) {
        NapAlarmScheduler.cancel(this);
        if (source != null) {
            try { source.stop(); } catch (Throwable ignored) { }
        }
        if (logger != null) logger.event(System.currentTimeMillis(), reason);
        active = false;
        closeLogger();
        store.finish(NapState.CANCELLED, reason);
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    private void closeLogger() {
        if (logger != null) {
            logger.close();
            logger = null;
        }
    }

    private Notification monitorNotification(String text) {
        return new Notification.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_nap)
                .setContentTitle("Power Nap")
                .setContentText(text)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
    }

    private void updateNotification(String text) {
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .notify(NOTIFICATION_ID, monitorNotification(text));
    }

    private void createChannel() {
        NotificationChannel c = new NotificationChannel(CHANNEL, "Power Nap monitoring",
                NotificationManager.IMPORTANCE_LOW);
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
    }

    @Override
    public void onDestroy() {
        if (source != null && active) {
            try { source.stop(); } catch (Throwable ignored) { }
        }
        closeLogger();
        active = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
