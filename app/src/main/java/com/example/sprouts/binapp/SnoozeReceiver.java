package com.example.sprouts.binapp;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class SnoozeReceiver extends BroadcastReceiver {

    private Calendar calendar;
    public SnoozeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager notification = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String action = intent.getAction();

        if(action.equals("SNOOZE_ACTION")) {
            Log.v("SnoozeReceiver", "Pressed SNOOZE");


            setAlarm(context);
            notification.cancel(002);

            Toast toast = Toast.makeText(context.getApplicationContext(), "Snooze until " + calendar.HOUR + " " + calendar.MINUTE, Toast.LENGTH_SHORT);
            toast.show();

        } else if(action.equals("DISMISS_ACTION")) {
            Log.v("SnoozeReceiver","Pressed DISMISS");
            notification.cancel(002);
        }

    }

    private void setAlarm(Context context) {

        calendar = Calendar.getInstance();

        calendar.add(Calendar.HOUR, 1);

        Log.v("SnoozeReceiver","Snooze until " + calendar.toString());

        Intent myIntent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmMgr.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);

    }
}
