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
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SettingsFragment extends PreferenceFragment implements TimePickerDialog.OnTimeSetListener {

    private String firstDate;
    private boolean reminder;

    private PendingIntent pendingIntent;
    private AlarmManager alarmMgr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor edit = preferences.edit();


        // CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference();

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
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        new TimePickerDialog(getActivity(), this, hour, minute, true).show();

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor edit = preferences.edit();

        edit.putInt("alarmHour", hourOfDay);
        edit.putInt("alarmMin", minute);
        edit.commit();

        firstDate = preferences.getString("firstDate", "DEFAULT");
        reminder = preferences.getBoolean("pref_reminder", true);

        Intent myIntent = new Intent(getActivity(), MyReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);


        if ((firstDate != "DEFAULT") && (reminder == true) ) {
            setAlarm(firstDate, hourOfDay, minute);
        } else {
            alarmMgr.cancel(pendingIntent);

            String alarmTime = "Alarm not set";

            Toast toast = Toast.makeText(getActivity(), alarmTime, Toast.LENGTH_SHORT);

            toast.show();
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
        calendar.add(Calendar.DAY_OF_MONTH, -1); //should be -1


        SimpleDateFormat df1 = new SimpleDateFormat("dd");
        SimpleDateFormat df2 = new SimpleDateFormat("HH");
        SimpleDateFormat df3 = new SimpleDateFormat("mm");

        String day = df1.format(calendar.getTime());
        String hour = df2.format(calendar.getTime());
        String min = df3.format(calendar.getTime());

        String alarmTime = "Alarm set for " + day + " at " + hour + " " + min;

        Toast toast = Toast.makeText(getActivity(), alarmTime, Toast.LENGTH_SHORT);

        toast.show();
        alarmMgr.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);

    }
}
