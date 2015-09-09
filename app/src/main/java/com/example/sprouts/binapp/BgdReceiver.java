package com.example.sprouts.binapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BgdReceiver extends BroadcastReceiver {
    public BgdReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent updateIntent = new Intent(context, BackgroundService.class);

        context.startService(updateIntent);
    }
}
