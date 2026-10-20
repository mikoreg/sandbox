package com.example.powernapwatch.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.powernapwatch.R;
import com.example.powernapwatch.core.NapState;
import com.example.powernapwatch.sensors.SensorEngine;
import com.example.powernapwatch.sensors.SleepSignalSourceFactory;
import com.example.powernapwatch.service.NapAlarmService;
import com.example.powernapwatch.service.NapMonitoringService;
import com.example.powernapwatch.util.NapAlarmScheduler;
import com.example.powernapwatch.util.PermissionHelper;
import com.example.powernapwatch.util.SessionStore;
import com.example.powernapwatch.util.CsvSessionLogger;

import com.example.powernapwatch.util.UserPreferences;

import java.io.IOException;

import java.io.File;
import java.util.List;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final String TAG = "PowerNap";
    private static final int REQ_FOREGROUND = 100;
    private static final int REQ_BACKGROUND = 101;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private SessionStore store;
    private UserPreferences preferences;
    private TextView state;
    private TextView metrics;
    private TextView provider;
    private TextView logStatus;
    private TextView message;
    private Spinner engineSpinner;
    private Switch loggingSwitch;
    private Button startStopButton;
    private Button permissionsButton;
    private Button exactAlarmButton;
    private View feedbackPanel;
    private View engineTitle;
    private boolean submittingFeedback;
    private String feedbackError = "";
    private List<SensorEngine> engines;
    private boolean bindingControls;

    private final Runnable refresher = new Runnable() {
        @Override public void run() {
            refreshUi();
            handler.postDelayed(this, 1_000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        store = new SessionStore(this);
        preferences = new UserPreferences(this);

        state = findViewById(R.id.state);
        metrics = findViewById(R.id.metrics);
        provider = findViewById(R.id.provider);
        logStatus = findViewById(R.id.logStatus);
        message = findViewById(R.id.message);
        engineSpinner = findViewById(R.id.engineSpinner);
        loggingSwitch = findViewById(R.id.loggingSwitch);

        startStopButton = findViewById(R.id.startButton);
        permissionsButton = findViewById(R.id.permissionsButton);
        exactAlarmButton = findViewById(R.id.exactAlarmButton);
        feedbackPanel = findViewById(R.id.feedbackPanel);
        engineTitle = findViewById(R.id.engineTitle);
        Button feedbackYes = findViewById(R.id.feedbackYes);
        Button feedbackNo = findViewById(R.id.feedbackNo);
        feedbackYes.setOnClickListener(v -> saveFeedback(true));
        feedbackNo.setOnClickListener(v -> saveFeedback(false));

        bindSensorControls();
        Log.i(TAG, "MainActivity created");

        permissionsButton.setOnClickListener(v -> {
            if (PermissionHelper.hasCore(this)) {
                Toast.makeText(this, "Permissions already granted", Toast.LENGTH_SHORT).show();
                message.setText("Foreground + background sensor permissions granted.");
            } else {
                PermissionHelper.requestForeground(this, REQ_FOREGROUND);
            }
        });

        exactAlarmButton.setOnClickListener(v -> {
            if (NapAlarmScheduler.canScheduleExact(this)) {
                Toast.makeText(this, "Exact alarm already granted", Toast.LENGTH_SHORT).show();
                message.setText("Exact alarm permission is granted.");
            } else {
                requestExactAlarmAccess();
            }
        });

        startStopButton.setOnClickListener(v -> {
            SessionStore.Snapshot current = store.read();
            if (current.feedbackPending()) return;
            if (current.state() == NapState.ALARMING) {
                Intent alarm = new Intent(this, NapAlarmService.class);
                alarm.setAction(NapAlarmService.ACTION_STOP_ALARM);
                startService(alarm);
                message.setText("Stopping alarm...");
                handler.postDelayed(this::refreshUi, 300);
                return;
            }
            if (isSessionRunning(current.state())) {
                Intent monitor = new Intent(this, NapMonitoringService.class);
                monitor.setAction(NapMonitoringService.ACTION_STOP);
                startService(monitor);
                message.setText("Stopping monitoring...");
                handler.postDelayed(this::refreshUi, 300);
                return;
            }
            if (!PermissionHelper.hasCore(this)) {
                Toast.makeText(this, "Missing sensor permissions", Toast.LENGTH_SHORT).show();
                PermissionHelper.requestForeground(this, REQ_FOREGROUND);
                message.setText("Grant sensor permissions, then press START again.");
                return;
            }
            if (!NapAlarmScheduler.canScheduleExact(this)) {
                Toast.makeText(this, "Missing exact alarm permission", Toast.LENGTH_SHORT).show();
                requestExactAlarmAccess();
                message.setText("Grant exact-alarm access, then press START again.");
                return;
            }

            SensorEngine selected = selectedEngine();
            boolean record = loggingSwitch.isChecked();
            preferences.engine(selected);
            preferences.loggingEnabled(record);

            Intent i = new Intent(this, NapMonitoringService.class);
            i.setAction(NapMonitoringService.ACTION_START);
            i.putExtra(NapMonitoringService.EXTRA_ENGINE, selected.name());
            i.putExtra(NapMonitoringService.EXTRA_LOGGING, record);
            startForegroundService(i);
            startStopButton.setText("STOP");
            message.setText("Starting " + selected.label() + (record ? " with CSV logging..." : "..."));
        });
    }

    private void saveFeedback(boolean slept) {
        if (submittingFeedback) return;
        SessionStore.Snapshot current = store.read();
        if (!current.feedbackPending()) return;
        submittingFeedback = true;
        try {
            // Persist in the matching session CSV before acknowledging the answer in UI.
            CsvSessionLogger.appendFeedback(this, current.file(), slept, current.state().name());
            store.feedbackSaved(slept);
            feedbackError = "";
            Toast.makeText(this, "Feedback saved to session CSV", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Cannot persist sleep feedback", e);
            feedbackError = "Nie udało się zapisać CSV — spróbuj ponownie.";
            message.setText(feedbackError);
            Toast.makeText(this, "CSV write error — please retry", Toast.LENGTH_LONG).show();
        } finally {
            submittingFeedback = false;
            refreshUi();
        }
    }

    private void bindSensorControls() {
        engines = SleepSignalSourceFactory.supportedEngines();
        ArrayAdapter<SensorEngine> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, engines);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        engineSpinner.setAdapter(adapter);

        bindingControls = true;
        SensorEngine preferred = preferences.engine();
        int index = engines.indexOf(preferred);
        if (index < 0) index = 0;
        engineSpinner.setSelection(index);
        loggingSwitch.setChecked(preferences.loggingEnabled());
        bindingControls = false;

        engineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!bindingControls) preferences.engine(engines.get(position));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        loggingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingControls) preferences.loggingEnabled(isChecked);
        });
    }

    private SensorEngine selectedEngine() {
        Object selected = engineSpinner.getSelectedItem();
        return selected instanceof SensorEngine ? (SensorEngine) selected : SensorEngine.AUTO;
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(refresher);
        handler.post(refresher);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(refresher);
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_FOREGROUND) {
            boolean allGranted = grantResults.length > 0;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) allGranted = false;
            }
            if (allGranted && PermissionHelper.hasForeground(this)) {
                PermissionHelper.requestBackground(this, REQ_BACKGROUND);
            } else {
                Toast.makeText(this, "Some foreground permissions were denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQ_BACKGROUND) {
            boolean granted = PermissionHelper.hasBackground(this);
            message.setText(granted
                    ? "All sensor permissions granted. Press START."
                    : "Background heart-rate permission was denied.");
        }
    }

    private void requestExactAlarmAccess() {
        if (NapAlarmScheduler.canScheduleExact(this)) return;
        try {
            Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:" + getPackageName()));
            startActivity(i);
        } catch (Exception e) {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivity(i);
        }
    }

    private void refreshUi() {
        SessionStore.Snapshot s = store.read();
        state.setText(s.state().name());
        provider.setText("active: " + s.provider() + "\nselected: " + s.selectedEngine());

        String file = s.file().isEmpty() ? "--" : new File(s.file()).getName();
        logStatus.setText((s.loggingEnabled() ? "RAW + events: " : "events only: ") + file);

        if (s.message() != null && !s.message().isEmpty()) {
            message.setText(s.message());
        }

        String hr = Float.isNaN(s.heartRate()) ? "--" : String.format(Locale.ROOT, "%.0f", s.heartRate());
        String rmssd = Float.isNaN(s.rmssd()) ? "--" : String.format(Locale.ROOT, "%.0f", s.rmssd());
        String motion = Float.isNaN(s.movement()) ? "--" : String.format(Locale.ROOT, "%.3f", s.movement());
        String text = String.format(Locale.ROOT,
                "HR %s | RMSSD %s ms\nMotion %s | confidence %.0f%%",
                hr, rmssd, motion, s.confidence() * 100.0);

        if (s.alarmAtMs() > 0 && s.alarmAtMs() > System.currentTimeMillis()) {
            long sec = (s.alarmAtMs() - System.currentTimeMillis()) / 1000;
            text += String.format(Locale.ROOT, "\nAlarm in %d:%02d", sec / 60, sec % 60);
        }
        metrics.setText(text);

        boolean running = isSessionRunning(s.state()) || s.state() == NapState.ALARMING;
        boolean waitingForFeedback = s.feedbackPending();
        startStopButton.setText(running ? "STOP" : "START");
        startStopButton.setVisibility(waitingForFeedback ? View.GONE : View.VISIBLE);
        feedbackPanel.setVisibility(waitingForFeedback ? View.VISIBLE : View.GONE);
        engineSpinner.setEnabled(!running && !waitingForFeedback);
        loggingSwitch.setEnabled(!running && !waitingForFeedback);
        // The two feedback buttons should be visible on a round screen without scrolling.
        int settingsVisibility = waitingForFeedback ? View.GONE : View.VISIBLE;
        engineTitle.setVisibility(settingsVisibility);
        engineSpinner.setVisibility(settingsVisibility);
        loggingSwitch.setVisibility(settingsVisibility);
        provider.setVisibility(settingsVisibility);
        logStatus.setVisibility(settingsVisibility);
        metrics.setVisibility(settingsVisibility);
        permissionsButton.setVisibility(settingsVisibility);
        exactAlarmButton.setVisibility(settingsVisibility);
        if (waitingForFeedback) {
            state.setText("SESJA ZAKOŃCZONA");
            message.setText(feedbackError.isEmpty()
                    ? "Twoja odpowiedź zostanie dopisana do CSV sesji."
                    : feedbackError);
        }
    }

    private static boolean isSessionRunning(NapState state) {
        return isMonitoringState(state) || state == NapState.NAPPING;
    }

    private static boolean isMonitoringState(NapState state) {
        return state == NapState.BASELINE
                || state == NapState.WAITING_FOR_SLEEP
                || state == NapState.SLEEP_CANDIDATE
                || state == NapState.SLEEP_CONFIRMED;
    }
}
