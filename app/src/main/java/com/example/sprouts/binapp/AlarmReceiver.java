package com.example.sprouts.binapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.v("My Receiver", "Ready");

        Intent robinIntent = new Intent(context, AlarmService.class);

        context.startService(robinIntent);

    }
}
