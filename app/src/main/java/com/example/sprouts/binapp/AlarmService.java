package com.example.sprouts.binapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.net.URI;

public class AlarmService extends Service {

    private NotificationManager mManager;
    private String bin;
    private int col;

    public AlarmService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.v("SIMPLESERVICE", "onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String binString = prefs.getString("bins", "DEFAULT");

        if (binString.contains(";")) {

            String[] bins = TextUtils.split(binString, ";");
            bin = bins[1];

            if (bin.contains("General")) {
                col = 0xFF888888; // grey
            } else {
                col = 0xFF00AA00; // green
            }


        }

        showNotification(col);

        Log.v("SIMPLESERVICE", "onStartCommand");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();

        Log.v("AlarmService", "onDestroy");
    }

    public void showNotification(int col) {

        PendingIntent launchIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        Intent snoozeIntent = new Intent(this, SnoozeReceiver.class);
        snoozeIntent.setAction("SNOOZE_ACTION");
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(this, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dismissIntent = new Intent(this, SnoozeReceiver.class);
        dismissIntent.setAction("DISMISS_ACTION");
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        int mNotificationId = 002;

        Notification.BigTextStyle big = new Notification.BigTextStyle();

        big.setBigContentTitle("Bin day tomorrow")
                .bigText(bin);

        Notification.Builder builder = new Notification.Builder(this);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        builder.setStyle(big)
                .setSmallIcon(R.drawable.ic_stat_bin_notification)
                .setColor(col)
                .setContentTitle("Bin day tomorrow")
                .setContentText(bin)
                .setShowWhen(true)
                .setContentIntent(launchIntent)
                .setSound(alarmSound)
                .addAction(R.drawable.ic_alarm_black_24dp, "Snooze", snoozePendingIntent)
                .addAction(R.drawable.ic_close_black_24dp, "Dismiss", dismissPendingIntent);
                ;
                // .setVibrate(new long[]{500, 500})
                // .addAction(R.drawable.ic_alarm_black_24dp, "Snooze", snooze)
        ;

        // TODO: Add cancel and remove buttons - snooze adds an hour and cancel sets alarm for following week...

        NotificationManager mNot = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNot.notify(mNotificationId, builder.build());
    }
}
