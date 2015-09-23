package com.example.sprouts.binapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class WakeReceiver extends BroadcastReceiver {
    public WakeReceiver() {
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {

            restartBgService(context);
            restartAlarm(context);

            Log.v("WakeReceiver", "restart alarms");

        }

    }


    private void restartBgService(Context context) {

        long interval = AlarmManager.INTERVAL_HALF_DAY;

        Log.v("MainActivity", "setBgdTask");

        Intent myIntent = new Intent(context, BgdReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, interval, pendingIntent);

    }

    private void restartAlarm(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean reminder = preferences.getBoolean("pref_reminder", true);
        String firstDate = preferences.getString("firstDate", "DEFAULT");
        int hour = preferences.getInt("alarmHour", 18);
        int minute = preferences.getInt("alarmMin", 0);

        if ((firstDate != "DEFAULT") && (reminder == true)) {
            setAlarm(context, firstDate, hour, minute);
            Log.v("ALARM", "SET");
        }

    }

    private void setAlarm(Context context, String date, int alarmHour, int alarmMin) {


        Calendar calendar = setCalendar(date, alarmHour, alarmMin);

        Intent myIntent = new Intent(context, MyReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);

    }

    private Calendar setCalendar(String date, int alarmHour, int alarmMin) {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat inputFormat = new SimpleDateFormat("EEEE d MMMM yyyy");

        Date alarmDate = null;

        try {
            alarmDate = inputFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        calendar.setTime(alarmDate);
        calendar.set(Calendar.HOUR_OF_DAY, alarmHour);
        calendar.set(Calendar.MINUTE, alarmMin);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, -1); // Set for the day before bin day

        return calendar;
    }
}
