package com.example.powernapwatch.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.powernapwatch.core.NapState;
import com.example.powernapwatch.util.SessionStore;

public final class NapAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("PowerNap", "NapAlarmReceiver onReceive: Alarm fired! Starting NapAlarmService...");
        new SessionStore(context).state(NapState.ALARMING, "Wake up");
        Intent service = new Intent(context, NapAlarmService.class);
        service.setAction(NapAlarmService.ACTION_ALARM);
        context.startForegroundService(service);
    }
}
