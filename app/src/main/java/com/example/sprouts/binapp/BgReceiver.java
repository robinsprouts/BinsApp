package com.example.sprouts.binapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BgReceiver extends BroadcastReceiver {
    public BgReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent updateIntent = new Intent(context, BgService.class);

        Log.v("BgReceiver", "onReceive");

        context.startService(updateIntent);
    }
}
