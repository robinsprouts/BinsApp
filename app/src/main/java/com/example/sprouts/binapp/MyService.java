package com.example.sprouts.binapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.Toast;

public class MyService extends Service {

    private NotificationManager mManager;
    private String bin;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();



        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String binString = prefs.getString("bins", "DEFAULT");

        if (binString.contains(";")) {

            String[] bins = TextUtils.split(binString, ";");
            bin = bins[1];
        }

    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        int mNotificationId = 001;
        Notification notify = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_bin_notification)
                .setColor(0xFF00AA00)
                .setContentTitle("Bin day tomorrow")
                .setContentText(bin)
                .setShowWhen(true)
                .build();


        NotificationManager mNot = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNot.notify(mNotificationId, notify);

        /*
        mManager = (NotificationManager) this.getApplicationContext().getSystemService(this.getApplicationContext().NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(this.getApplicationContext(),MainActivity.class);

        Notification notification = new Notification(R.drawable.notification_template_icon_bg,"This is a test message!", System.currentTimeMillis());
        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingNotificationIntent = PendingIntent.getActivity( this.getApplicationContext(),0, intent1,PendingIntent.FLAG_UPDATE_CURRENT);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.setLatestEventInfo(this.getApplicationContext(), "AlarmManagerDemo", "This is a test message!", pendingNotificationIntent);

        mManager.notify(0, notification);

        */
    }

    @Override
    public void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}
