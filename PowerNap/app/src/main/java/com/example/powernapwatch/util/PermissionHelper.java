package com.example.powernapwatch.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public final class PermissionHelper {
    private static final String READ_HEART_RATE = "android.permission.health.READ_HEART_RATE";
    private static final String READ_HEALTH_DATA_IN_BACKGROUND =
            "android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND";

    private PermissionHelper() {}

    /**
     * Android 16 / API 36 replaced BODY_SENSORS with granular health permissions
     * for apps targeting API 36. Older Wear OS releases still use BODY_SENSORS.
     */
    public static String heartPermission() {
        if (Build.VERSION.SDK_INT >= 36) return READ_HEART_RATE;
        return Manifest.permission.BODY_SENSORS;
    }

    public static String backgroundHeartPermission() {
        if (Build.VERSION.SDK_INT >= 36) return READ_HEALTH_DATA_IN_BACKGROUND;
        if (Build.VERSION.SDK_INT >= 33) return Manifest.permission.BODY_SENSORS_BACKGROUND;
        return null;
    }

    public static boolean hasForeground(Activity a) {
        if (a.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED)
            return false;
        if (a.checkSelfPermission(heartPermission()) != PackageManager.PERMISSION_GRANTED)
            return false;
        return Build.VERSION.SDK_INT < 33
                || a.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasBackground(Activity a) {
        String bg = backgroundHeartPermission();
        return bg == null || a.checkSelfPermission(bg) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasCore(Activity a) {
        return hasForeground(a) && hasBackground(a);
    }

    public static void requestForeground(Activity a, int requestCode) {
        List<String> permissions = new ArrayList<>();
        if (a.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION);
        if (a.checkSelfPermission(heartPermission()) != PackageManager.PERMISSION_GRANTED)
            permissions.add(heartPermission());
        if (Build.VERSION.SDK_INT >= 33
                && a.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);

        if (!permissions.isEmpty()) {
            a.requestPermissions(permissions.toArray(new String[0]), requestCode);
        } else {
            requestBackground(a, requestCode + 1);
        }
    }

    public static void requestBackground(Activity a, int requestCode) {
        String bg = backgroundHeartPermission();
        if (bg != null && a.checkSelfPermission(bg) != PackageManager.PERMISSION_GRANTED) {
            a.requestPermissions(new String[]{bg}, requestCode);
        }
    }
}
