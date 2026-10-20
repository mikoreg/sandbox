package com.example.powernapwatch.detection;

public enum DetectionEngine {
    AUTO("Auto (Wear OS sleep -> Legacy fallback)"),
    WEAR_OS_SLEEP("Wear OS sleep state"),
    LEGACY_HR_ACC("Legacy HR + ACC");

    private final String label;

    DetectionEngine(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static DetectionEngine fromName(String value) {
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
