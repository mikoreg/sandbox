package com.example.powernapwatch.sensors;

import android.content.Context;

import java.util.List;

public final class SleepSignalSourceFactory {
    private SleepSignalSourceFactory() { }

    public static SleepSignalSource create(Context context, SensorEngine engine) {
        return switch (engine) {
            case WEAR_OS -> new StandardWearSensorSource(context);
            case SAMSUNG_HEALTH -> new SamsungHealthSensorSource(context, false);
            case AUTO -> new SamsungHealthSensorSource(context, true);
        };
    }

    public static List<SensorEngine> supportedEngines() {
        return List.of(SensorEngine.AUTO, SensorEngine.SAMSUNG_HEALTH, SensorEngine.WEAR_OS);
    }
}
