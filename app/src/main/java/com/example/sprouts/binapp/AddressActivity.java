package com.example.sprouts.binapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.Calendar;

public class AddressActivity extends AppCompatActivity implements AddressDialogFragment.OnCompleteListener  {

    private EditText address;
    private UpdateReceiver updateReceiver;
    private PendingIntent pendingIntent;
    private AlarmManager alarmMgr;
    private boolean complete;

    private String firstDate;
    private String secondDate;
    int hourPref;
    int minPref;
    boolean reminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        complete = false;
        address = (EditText) findViewById(R.id.address1);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        firstDate = preferences.getString("firstDate", "DEFAULT");
        secondDate = preferences.getString("secondDate", "DEFAULT");
        hourPref = preferences.getInt("alarmHour", 18);
        minPref = preferences.getInt("alarmMin", 0);
        reminder = preferences.getBoolean("pref_reminder", true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateReceiver = new UpdateReceiver();

        IntentFilter intentFilter = new IntentFilter("FINISHED"); // somehow make this what delays return to first activity

        registerReceiver(updateReceiver, intentFilter);

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

        long interval = AlarmManager.INTERVAL_HALF_DAY/2;

        // interval = 1000;

        Log.v("MainActivity", "setBgdTask");

        Intent updateIntent = new Intent(this, BgService.class);

        this.startService(updateIntent); // gets the bin data for the first time

        Intent myIntent = new Intent(AddressActivity.this, BgdReceiver.class);

        pendingIntent = PendingIntent.getBroadcast(AddressActivity.this, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);

        // launches the background bin update service
        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, interval, pendingIntent);

    }


    private class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("FINISHED")) {

                Bundle extras = intent.getExtras();

                String error = extras.getString("error");

                Log.v("AddressActivity", error);


                if (complete) {

                    Log.v("AddressActivity", "updated");

                    Calendar calendar = AlarmSet.setCalendar(firstDate, hourPref, minPref);
                    Calendar current = Calendar.getInstance();

                    if ((firstDate != "DEFAULT") && (reminder)) {

                        if (calendar.before(current)) {
                            calendar = AlarmSet.setCalendar(secondDate, hourPref, minPref); // set calendar to day before bin day
                        } else {
                            calendar = AlarmSet.setCalendar(firstDate, hourPref, minPref);
                        }
                    }

                    setAlarm(calendar); // sets the alarm


                    finish();
                }

            }

        }
    }

    private void setAlarm(Calendar calendar) {

        Intent myIntent = new Intent(AddressActivity.this, MyReceiver.class);

        pendingIntent = PendingIntent.getBroadcast(AddressActivity.this, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmMgr.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);

        Log.v("AddressActivity", "alarm set " + firstDate + " " + hourPref + " " + minPref);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_address, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
