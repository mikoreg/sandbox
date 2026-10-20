package com.example.powernapwatch.sensors;

public enum SensorEngine {
    AUTO("Auto (Samsung -> Wear OS fallback)"),
    SAMSUNG_HEALTH("Samsung Health Sensor SDK"),
    WEAR_OS("Wear OS SensorManager");

    private final String label;

    SensorEngine(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static SensorEngine fromName(String value) {
        if (value == null) return AUTO;
        try {
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            return AUTO;
        }
    }

    @Override
    public String toString() {
        return label;
    }
}
