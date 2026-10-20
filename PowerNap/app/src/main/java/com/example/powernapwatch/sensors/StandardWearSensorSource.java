package com.example.powernapwatch.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

/** Portable Wear OS fallback: Android accelerometer + TYPE_HEART_RATE. */
public final class StandardWearSensorSource implements SleepSignalSource, SensorEventListener {

    private final SensorManager manager;
    private final long bootToWallOffsetMs;
    private Listener listener;
    private Sensor accelerometer;
    private Sensor heartRate;
    private Sensor offBody;

    public StandardWearSensorSource(Context context) {
        manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        bootToWallOffsetMs = System.currentTimeMillis() - SystemClock.elapsedRealtime();
    }

    @Override
    public String name() {
        return "Wear OS SensorManager";
    }

    @Override
    public void start(Listener listener) {
        this.listener = listener;
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        heartRate = manager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        offBody = manager.getDefaultSensor(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT);

        Log.i("PowerNap", "StandardWearSensorSource start: accel=" + (accelerometer != null) + " hr=" + (heartRate != null) + " offBody=" + (offBody != null));

        if (accelerometer == null || heartRate == null) {
            Log.e("PowerNap", "StandardWearSensorSource error: accel or hr missing!");
            listener.onSourceError("Required accelerometer/heart-rate sensor is missing", null);
            return;
        }

        // ~25 Hz. maxReportLatency=0 requests immediate delivery.
        manager.registerListener(this, accelerometer, 40_000, 0);
        manager.registerListener(this, heartRate, SensorManager.SENSOR_DELAY_NORMAL, 0);
        if (offBody != null) manager.registerListener(this, offBody, SensorManager.SENSOR_DELAY_NORMAL, 0);
        Log.i("PowerNap", "Registered Standard Wear sensors listeners");
        listener.onSourceStatus("Standard Wear sensors active");
    }

    @Override
    public void stop() {
        manager.unregisterListener(this);
        listener = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Listener l = listener;
        if (l == null) return;
        long wallMs = bootToWallOffsetMs + event.timestamp / 1_000_000L;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER -> l.onAcceleration(
                    wallMs, event.values[0], event.values[1], event.values[2]);
            case Sensor.TYPE_HEART_RATE -> l.onHeartRate(wallMs, event.values[0], event.accuracy);
            case Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT -> l.onBodyState(wallMs, event.values[0] >= 0.5f);
            default -> { }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
