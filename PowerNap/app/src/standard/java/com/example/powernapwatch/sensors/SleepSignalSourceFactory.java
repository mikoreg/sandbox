package com.example.powernapwatch.sensors;

import android.content.Context;

import java.util.List;

public final class SleepSignalSourceFactory {
    private SleepSignalSourceFactory() { }

    public static SleepSignalSource create(Context context, SensorEngine engine) {
        if (engine == SensorEngine.SAMSUNG_HEALTH) {
            throw new IllegalArgumentException("Samsung Health Sensor SDK is not included in the standard build");
        }
        return new StandardWearSensorSource(context);
    }

    public static List<SensorEngine> supportedEngines() {
        return List.of(SensorEngine.AUTO, SensorEngine.WEAR_OS);
    }
}
