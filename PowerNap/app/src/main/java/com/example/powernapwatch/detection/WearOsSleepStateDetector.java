package com.example.powernapwatch.detection;

import android.content.Context;
import android.util.Log;

import androidx.health.services.client.HealthServices;
import androidx.health.services.client.PassiveListenerCallback;
import androidx.health.services.client.PassiveMonitoringClient;
import androidx.health.services.client.data.PassiveListenerConfig;
import androidx.health.services.client.data.PassiveMonitoringCapabilities;
import androidx.health.services.client.data.UserActivityInfo;
import androidx.health.services.client.data.UserActivityState;

import java.time.Instant;
import java.util.concurrent.Executor;

/**
 * Thin adapter around Wear OS Health Services user-activity state.
 *
 * Health Services may report the state change retrospectively, therefore the
 * event carries both stateChangeTime and the local receive time. The caller
 * must use stateChangeTime as the candidate sleep onset.
 */
public final class WearOsSleepStateDetector {
    private static final String TAG = "PowerNap-WearSleep";

    public interface Listener {
        void onCapability(boolean supported, String detail);
        void onActivityState(UserActivityState state, long stateChangeTimeMs, long receivedTimeMs);
        void onRegistered();
        void onError(String message, Throwable error);
    }

    private final Context context;
    private final Listener listener;
    private final Executor executor;
    private PassiveMonitoringClient client;
    private volatile boolean stopped;
    private volatile boolean supported;
    private volatile boolean registrationRequested;

    private final PassiveListenerCallback callback = new PassiveListenerCallback() {
        @Override
        public void onRegistered() {
            if (stopped) return;
            listener.onRegistered();
        }

        @Override
        public void onRegistrationFailed(Throwable throwable) {
            if (stopped) return;
            listener.onError("Wear OS sleep-state registration failed", throwable);
        }

        @Override
        public void onPermissionLost() {
            if (stopped) return;
            listener.onError("Wear OS sleep-state permission lost", null);
        }

        @Override
        public void onUserActivityInfoReceived(UserActivityInfo info) {
            if (stopped || info == null) return;
            UserActivityState state = info.getUserActivityState();
            Instant change = info.getStateChangeTime();
            long changeMs = change == null ? System.currentTimeMillis() : change.toEpochMilli();
            listener.onActivityState(state, changeMs, System.currentTimeMillis());
        }
    };

    public WearOsSleepStateDetector(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        this.executor = context.getMainExecutor();
    }

    public void start() {
        stopped = false;
        try {
            client = HealthServices.getClient(context).getPassiveMonitoringClient();
            var future = client.getCapabilitiesAsync();
            future.addListener(() -> {
                if (stopped) return;
                try {
                    PassiveMonitoringCapabilities capabilities = future.get();
                    supported = capabilities.getSupportedUserActivityStates()
                            .contains(UserActivityState.USER_ACTIVITY_ASLEEP);
                    listener.onCapability(supported,
                            supported ? "USER_ACTIVITY_ASLEEP supported"
                                    : "USER_ACTIVITY_ASLEEP not supported");
                    if (supported) registerCallback();
                } catch (Throwable t) {
                    listener.onCapability(false, "Capability check failed: " + safeMessage(t));
                    listener.onError("Wear OS sleep-state capability check failed", t);
                }
            }, executor);
        } catch (Throwable t) {
            supported = false;
            listener.onCapability(false, "Health Services unavailable: " + safeMessage(t));
            listener.onError("Wear OS Health Services unavailable", t);
        }
    }

    private void registerCallback() {
        if (stopped || client == null || !supported) return;
        try {
            PassiveListenerConfig config = new PassiveListenerConfig.Builder()
                    .setShouldUserActivityInfoBeRequested(true)
                    .build();
            registrationRequested = true;
            client.setPassiveListenerCallback(config, executor, callback);
        } catch (Throwable t) {
            listener.onError("Wear OS sleep-state callback setup failed", t);
        }
    }

    public boolean isSupported() {
        return supported;
    }

    public void stop() {
        stopped = true;
        PassiveMonitoringClient c = client;
        client = null;
        if (c != null && registrationRequested) {
            try {
                c.clearPassiveListenerCallbackAsync();
            } catch (Throwable t) {
                Log.w(TAG, "Unable to clear passive callback", t);
            }
        }
        registrationRequested = false;
    }

    private static String safeMessage(Throwable t) {
        if (t == null || t.getMessage() == null || t.getMessage().isBlank()) {
            return t == null ? "unknown" : t.getClass().getSimpleName();
        }
        return t.getMessage();
    }
}
