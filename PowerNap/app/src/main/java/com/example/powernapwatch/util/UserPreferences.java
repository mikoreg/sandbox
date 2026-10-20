package com.example.powernapwatch.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.powernapwatch.sensors.SensorEngine;

public final class UserPreferences {
    private static final String PREFS = "power_nap_preferences";
    private static final String KEY_ENGINE = "sensor_engine";
    private static final String KEY_LOGGING = "record_sensor_data";

    private final SharedPreferences preferences;

    public UserPreferences(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public SensorEngine engine() {
        return SensorEngine.fromName(preferences.getString(KEY_ENGINE, SensorEngine.AUTO.name()));
    }

    public void engine(SensorEngine engine) {
        preferences.edit().putString(KEY_ENGINE, engine.name()).apply();
    }

    public boolean loggingEnabled() {
        return preferences.getBoolean(KEY_LOGGING, true);
    }

    public void loggingEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_LOGGING, enabled).apply();
    }
}
