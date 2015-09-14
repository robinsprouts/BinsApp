package com.example.sprouts.binapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.prefs.Preferences;

public class SettingsFragment extends PreferenceFragment implements TimePickerDialog.OnTimeSetListener {

    private String firstDate;
    private boolean reminder;
    private int hourPref;

    private PendingIntent pendingIntent;
    private AlarmManager alarmMgr;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        Preference timePref = findPreference("time_pick");
        timePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showTimeDialog();
                return false;
            }
        });
    }

    public void showTimeDialog() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        hourPref = preferences.getInt("alarmHour", 18);

        int hour = hourPref;
        int minute = 0;

        new TimePickerDialog(getActivity(), this, hour, minute, true).show();

    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor edit = preferences.edit();

        edit.putInt("alarmHour", hourOfDay);
        edit.putInt("alarmMin", minute);
        edit.commit();

        Intent updateIntent = new Intent(getActivity(), BgService.class);

        Log.v("SettingsFragment", "runIntent");

        getActivity().startService(updateIntent);


        firstDate = preferences.getString("firstDate", "DEFAULT");
        reminder = preferences.getBoolean("pref_reminder", true);

        Intent myIntent = new Intent(getActivity(), MyReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);


        if ((firstDate != "DEFAULT") && (reminder == true) ) {
            setAlarm(firstDate, hourOfDay, minute);
            Log.v("ALARM", "SET");

        } else {
            alarmMgr.cancel(pendingIntent);

            Log.v("ALARM", "NOT SET");
        }



    }


    private void setAlarm(String date, int alarmHour, int alarmMin) {

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

        SimpleDateFormat fDate = new SimpleDateFormat("EEEE d MMMM");
        SimpleDateFormat fTime = new SimpleDateFormat("HH:mm");

        String alarmTime = fDate.format(calendar.getTime()) + " at " + fTime.format(calendar.getTime());

        alarmMgr.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);

        Preference timePref = findPreference("time_pick");

        timePref.setSummary(alarmTime);
    }
}
