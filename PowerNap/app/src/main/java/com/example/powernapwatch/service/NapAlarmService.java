package com.example.powernapwatch.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;

import com.example.powernapwatch.R;
import com.example.powernapwatch.core.NapState;
import com.example.powernapwatch.ui.MainActivity;
import com.example.powernapwatch.util.SessionStore;

public final class NapAlarmService extends Service {
    public static final String ACTION_ALARM = "com.example.powernapwatch.ALARM";
    public static final String ACTION_STOP_ALARM = "com.example.powernapwatch.STOP_ALARM";
    private static final String CHANNEL = "power_nap_alarm";
    private static final int NOTIFICATION_ID = 1201;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("PowerNap", "NapAlarmService onCreate");
        NotificationChannel c = new NotificationChannel(CHANNEL, "Power Nap alarm",
                NotificationManager.IMPORTANCE_HIGH);
        c.setVibrationPattern(new long[]{0, 200, 250, 400, 250, 800});
        c.enableVibration(true);
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("PowerNap", "NapAlarmService onStartCommand action=" + (intent == null ? "null" : intent.getAction()));
        if (intent != null && ACTION_STOP_ALARM.equals(intent.getAction())) {
            stopAlarm();
            return START_NOT_STICKY;
        }

        Notification notification = alarmNotification();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
        vibrate();
        handler.postDelayed(this::stopAlarm, 2 * 60_000L);
        return START_NOT_STICKY;
    }

    private Notification alarmNotification() {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent openPi = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stop = new Intent(this, StopAlarmReceiver.class);
        PendingIntent stopPi = PendingIntent.getBroadcast(this, 1, stop,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new Notification.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_nap)
                .setContentTitle("Power Nap — pobudka")
                .setContentText("Minęło 10 minut snu")
                .setCategory(Notification.CATEGORY_ALARM)
                .setOngoing(true)
                .setContentIntent(openPi)
                .addAction(new Notification.Action.Builder(null, "STOP", stopPi).build())
                .build();
    }

    private void vibrate() {
        long[] pattern = {0, 200, 250, 400, 250, 800, 500};
        VibrationEffect effect = VibrationEffect.createWaveform(pattern, 0);
        if (Build.VERSION.SDK_INT >= 31) {
            VibratorManager vm = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vm.getDefaultVibrator().vibrate(effect);
        } else {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(effect);
        }
    }

    private void cancelVibration() {
        if (Build.VERSION.SDK_INT >= 31) {
            ((VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE))
                    .getDefaultVibrator().cancel();
        } else {
            ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).cancel();
        }
    }

    private void stopAlarm() {
        cancelVibration();
        handler.removeCallbacksAndMessages(null);
        new SessionStore(this).finish(NapState.FINISHED, "Alarm stopped — report whether you actually slept");
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
