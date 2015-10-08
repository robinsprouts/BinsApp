package com.example.sprouts.binapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Calendar;

public class AddressActivity extends AppCompatActivity implements AddressDialogFragment.OnCompleteListener  {

    private SharedPreferences preferences;
    private EditText address;
    private UpdateReceiver updateReceiver;
    private PendingIntent pendingIntent;
    private boolean complete;

    private String firstDate;
    private String secondDate;
    int hourPref;
    int minPref;
    boolean reminder;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        complete = false;
        address = (EditText) findViewById(R.id.address1);


        preferences = PreferenceManager.getDefaultSharedPreferences(AddressActivity.this);
        firstDate = preferences.getString("firstDate", "DEFAULT");
        secondDate = preferences.getString("secondDate", "DEFAULT");
        hourPref = preferences.getInt("alarmHour", 18);
        minPref = preferences.getInt("alarmMin", 0);
        reminder = preferences.getBoolean("pref_reminder", true);

        // Obtain the shared Tracker instance.

        AnalyticApp application = (AnalyticApp) getApplication();
        mTracker = application.getDefaultTracker();


    }

    @Override
    protected void onResume() {
        super.onResume();
        updateReceiver = new UpdateReceiver();

        IntentFilter intentFilter = new IntentFilter("FINISHED"); // somehow make this what delays return to first activity

        registerReceiver(updateReceiver, intentFilter);


        String name = "Address";
        Log.i("AddressActivity", "Setting screen name: " + name);
        mTracker.setScreenName("Screen = " + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }

    @Override
    public void onComplete(String string) {
        complete = true;
        address.setText(string);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AddressActivity.this);

        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString("address", string);
        edit.commit();

        setBgdTask(); // starts the background service to collect data


        View layoutView = findViewById(R.id.layout);

        Snackbar.make(layoutView, "Updating", Snackbar.LENGTH_INDEFINITE).show();

    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(updateReceiver);
    }

    private void setBgdTask() {

        long interval = AlarmManager.INTERVAL_DAY;

        Log.v("MainActivity", "setBgdTask");

        Intent updateIntent = new Intent(this, BgService.class);

        this.startService(updateIntent); // gets the bin data for the first time

        Intent myIntent = new Intent(AddressActivity.this, BgReceiver.class);

        pendingIntent = PendingIntent.getBroadcast(AddressActivity.this, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);

        // launches the background bin update service
        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, interval, pendingIntent);

    }


    private class UpdateReceiver extends BroadcastReceiver {

        // SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AddressActivity.this);

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("FINISHED")) {

                Bundle extras = intent.getExtras();

                String error = extras.getString("error");

                Log.v("AddressActivity", error);

                /*
                firstDate = preferences.getString("firstDate", "DEFAULT");
                secondDate = preferences.getString("secondDate", "DEFAULT");
                hourPref = preferences.getInt("alarmHour", 18);
                minPref = preferences.getInt("alarmMin", 0);
                reminder = preferences.getBoolean("pref_reminder", true);


                if (complete) {

                    Log.v("AddressActivity", "updated");

                    Calendar calendar = AlarmSet.setCalendar(firstDate, hourPref, minPref);
                    Calendar current = Calendar.getInstance();

                    if ((!firstDate.equals("DEFAULT")) && (reminder)) {

                        Log.v("AddressActivity", "Alarm set for first date: " + firstDate);

                        if (calendar.before(current)) {
                            calendar = AlarmSet.setCalendar(secondDate, hourPref, minPref); // set calendar to day before bin day
                        } else {
                            calendar = AlarmSet.setCalendar(firstDate, hourPref, minPref);
                        }
                    }

                    setAlarm(calendar); // sets the alarm

*/
                    finish();
                }

            }

        }

    private void setAlarm(Calendar calendar) {

        Intent myIntent = new Intent(AddressActivity.this, AlarmReceiver.class);

        pendingIntent = PendingIntent.getBroadcast(AddressActivity.this, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmMgr.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);

        Log.v("AddressActivity", "alarm set " + firstDate + " " + hourPref + " " + minPref);

    }

}
