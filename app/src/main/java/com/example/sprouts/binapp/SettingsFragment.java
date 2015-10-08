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

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SettingsFragment extends PreferenceFragment implements TimePickerDialog.OnTimeSetListener {

    private String firstDate;
    private String secondDate;
    private boolean reminder;
    private int hourPref;
    private int minPref;

    private PendingIntent pendingIntent;
    private AlarmManager alarmMgr;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Preference timePref = findPreference("time_pick");
        firstDate = preferences.getString("firstDate", "DEFAULT");
        secondDate = preferences.getString("secondDate", "DEFAULT");
        hourPref = preferences.getInt("alarmHour", 18);
        minPref = preferences.getInt("alarmMin", 0);


        Calendar calendar = AlarmSet.setCalendar(firstDate, hourPref, minPref); // sets calendar to day before bin day

        Calendar current = Calendar.getInstance();

        if (calendar.before(current)) {
            calendar = AlarmSet.setCalendar(secondDate, hourPref, minPref);
        }
            setSummary(calendar);

        timePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showTimeDialog(hourPref, minPref);
                return false;
            }
        });
    }

    public void showTimeDialog(int hour, int minute) {

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
        secondDate = preferences.getString("secondDate", "DEFAULT");
        reminder = preferences.getBoolean("pref_reminder", true);

        Intent myIntent = new Intent(getActivity(), AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = AlarmSet.setCalendar(firstDate, hourOfDay, minute);

        Calendar current = Calendar.getInstance();


        if ((firstDate != "DEFAULT") && (reminder)) {

            if (calendar.before(current)) {
                calendar = AlarmSet.setCalendar(secondDate, hourOfDay, minute); // set calendar to day before bin day
            } else {
                calendar = AlarmSet.setCalendar(firstDate, hourOfDay, minute);
            }
            setAlarm(calendar);
            setSummary(calendar);
            Log.v("ALARM", "SET");

        } else {
            alarmMgr.cancel(pendingIntent); // separate out reminder bit

            Log.v("ALARM", "NOT SET");
        }


    }

    private void setSummary(Calendar calendar) {

        SimpleDateFormat fDate = new SimpleDateFormat("EEEE d MMMM");
        SimpleDateFormat fTime = new SimpleDateFormat("HH:mm");

        String alarmTime = fDate.format(calendar.getTime()) + " at " + fTime.format(calendar.getTime());
        Preference timePref = findPreference("time_pick");

        timePref.setSummary(alarmTime);
    }

    private void setAlarm(Calendar calendar) {

        alarmMgr.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);

    }


}