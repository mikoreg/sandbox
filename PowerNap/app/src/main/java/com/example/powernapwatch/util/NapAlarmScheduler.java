package com.example.powernapwatch.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.powernapwatch.service.NapAlarmReceiver;

public final class NapAlarmScheduler {
    private static final int REQUEST_CODE = 4010;
    private NapAlarmScheduler() { }

    public static boolean canScheduleExact(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        return am != null && (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms());
    }

    public static void schedule(Context context, long wallClockMs) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = alarmIntent(context);
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, wallClockMs, pi);
    }

    public static void cancel(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        am.cancel(alarmIntent(context));
    }

    private static PendingIntent alarmIntent(Context context) {
        Intent i = new Intent(context, NapAlarmReceiver.class);
        return PendingIntent.getBroadcast(context, REQUEST_CODE, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
