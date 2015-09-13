package com.example.sprouts.binapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BgdReceiver extends BroadcastReceiver {
    public BgdReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent updateIntent = new Intent(context, BgService.class);

        Log.v("BgdReceiver", "onReceive");

        context.startService(updateIntent);
    }
}
