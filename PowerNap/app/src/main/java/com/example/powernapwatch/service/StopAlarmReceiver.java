package com.example.powernapwatch.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class StopAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, NapAlarmService.class);
        i.setAction(NapAlarmService.ACTION_STOP_ALARM);
        context.startService(i);
    }
}
