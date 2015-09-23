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

public class AddressActivity extends AppCompatActivity implements AddressDialogFragment.OnCompleteListener  {

    private EditText address;
    private UpdateReceiver updateReceiver;
    private PendingIntent pendingIntent;
    private AlarmManager alarmMgr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        address = (EditText) findViewById(R.id.address1);

    }

    @Override
    public void onComplete(String string) {
        address.setText(string);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AddressActivity.this);

        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString("address", string);
        edit.commit();

        setBgdTask();

        updateReceiver = new UpdateReceiver();

        IntentFilter intentFilter = new IntentFilter("FINISHED"); // somehow make this what delays return to first activity

        registerReceiver(updateReceiver, intentFilter);

        View layoutView = findViewById(R.id.layout);
        Snackbar.make(layoutView, string, Snackbar.LENGTH_LONG).show();

        long w = 1000;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, w);

    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(updateReceiver);
    }

    private void launchIntent() {
        Intent updateIntent = new Intent(this, BgService.class);

        Log.v("SettingsFragment", "runIntent");

        this.startService(updateIntent);

    }

    private void setBgdTask() {

        long interval = AlarmManager.INTERVAL_HALF_DAY/2;

        // interval = 1000;

        Log.v("MainActivity", "setBgdTask");

        Intent myIntent = new Intent(AddressActivity.this, BgdReceiver.class);

        pendingIntent = PendingIntent.getBroadcast(AddressActivity.this, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, interval, pendingIntent);

    }

    private class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorView);

            if (intent.getAction().equals("FINISHED")) {

                Bundle extras = intent.getExtras();

                String error = extras.getString("error");

                Snackbar.make(coordinatorLayout, error, Snackbar.LENGTH_SHORT).show();

                SharedPreferences prefs;

                prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String binString = prefs.getString("bins", "DEFAULT");

            }

        }
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
