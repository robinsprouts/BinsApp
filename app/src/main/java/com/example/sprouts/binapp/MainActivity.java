package com.example.sprouts.binapp;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.support.design.widget.*;
import android.support.v7.app.*;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.jaunt.*;
import java.io.*;
import java.text.*;
import java.util.*;

import java.text.ParseException;

/* TO DO: I need to update the binList each time the address is refreshed (in AddressActivity) *and* in the MainActivity when the app first opens, and at regular intervals using an alarm

I also need to convert the dates into proper dates so that I can make the alarm work... Don't use alarm use jobScheduler instead

 */

public class MainActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "Bins";
    private TextView postText;
    private TextView firedText;
    private String fullAddress;
    private String firstDate;
    private String lastFired;
    private boolean reminder;
    private int alarmHour;
    private int alarmMin;

    private UpdateReceiver updateReceiver;


    private AlarmManager alarmMgr;

    private PendingIntent pendingIntent;

    ArrayAdapter<String> arrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        postText = (TextView) findViewById(R.id.textView);

        firedText = (TextView) findViewById(R.id.textView2);

        Intent myIntent = new Intent(MainActivity.this, MyReceiver.class);

        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setBgdTask();

        //registerReceiver();

    }

    private class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("FINISHED")) {
                Toast.makeText(MainActivity.this, "FINISHED", Toast.LENGTH_SHORT).show();

                SharedPreferences prefs;

                prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String binString = prefs.getString("bins", "DEFAULT");

                if (binString.contains(";")) {

                    updateBinList(binString);
                    Toast.makeText(MainActivity.this, "UPDATING", Toast.LENGTH_LONG).show(); // keep this but as SnackBar
                }

            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateReceiver = new UpdateReceiver();

        IntentFilter intentFilter = new IntentFilter("FINISHED");

        registerReceiver(updateReceiver, intentFilter);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        fullAddress = prefs.getString("address", "DEFAULT");

        firstDate = prefs.getString("firstDate", "DEFAULT");

        reminder = prefs.getBoolean("pref_reminder", true);

        alarmHour = prefs.getInt("alarmHour", 18);

        alarmMin = prefs.getInt("alarmMin", 0);

        lastFired = prefs.getString("last_fired", "blank");

        firedText.setText(lastFired);


        if (fullAddress.contains(",")) {

            String shortAddress = shortenAddress(fullAddress);

            postText.setText(shortAddress);

            // check();

        } else {

            launchInput();

        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String binString = prefs.getString("bins", "DEFAULT");

        if (binString.contains(";")) {

            updateBinList(binString);

        } else {
            launchInput();

        }

        /* if (firstDate != "DEFAULT") {
            setAlarm(firstDate);
        } */

    }

    private void updateBinList(String binString) {

        String[] bins = TextUtils.split(binString, ";");

        ArrayList<String> binList = new ArrayList(Arrays.asList(bins));

        ListView listView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new MyAdapter(MainActivity.this, binList);
        listView.setAdapter(arrayAdapter);
    }

    private void setBgdTask() {

        Calendar calendar = Calendar.getInstance();

        long interval = AlarmManager.INTERVAL_HALF_DAY;

        // interval = 1000 * 60 * 2;

        Log.v("MainActivity", "setBgdTask");

        Intent myIntent = new Intent(MainActivity.this, BgdReceiver.class);

        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmMgr.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), interval, pendingIntent);

        // Change this to elapsedrealtime

    }


    private String convertDate(String binDate) {

        int length = binDate.length();
        String stringDate = binDate.substring(length - 10, length);

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE d MMMM yyyy");

        try {
            Date date = inputFormat.parse(stringDate);
            stringDate = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return stringDate;
    }

    private String shortenAddress(String address) {
        String shortAddress = address.substring(0, address.indexOf(","));

        String[] splitAddress = shortAddress.split(" ");

        for (int i = 0 ; i < (splitAddress.length); i++) {
            String first = splitAddress[i].substring(0,1);
            String second = splitAddress[i].substring(1,splitAddress[i].length());
            splitAddress[i] = first + second.toLowerCase();
        }

        shortAddress = TextUtils.join(" ", splitAddress);

        return shortAddress;
    }


    public void check() {

                String stringUrl = "https://wastemanagementcalendar.cardiff.gov.uk/English.aspx";
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    new DownloadWebpageTask().execute(stringUrl);
                }
            }

    private class DownloadWebpageTask extends AsyncTask<String, Void, ArrayList> {

        @Override
        protected ArrayList doInBackground(String... urls) {


            ArrayList<String> a = new ArrayList();

            try {
                return jaunty(urls[0]);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                return a;
            }
        }


        @Override
        protected void onPostExecute(ArrayList arrayList) {


            String joined = TextUtils.join(";", arrayList);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

            final SharedPreferences.Editor edit = prefs.edit();
            edit.putString("bins", joined);
            edit.putString("firstDate", firstDate);
            edit.commit();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(updateReceiver);
    }

    private ArrayList jaunty(String myurl) throws IOException {


        String[] bins = new String[2];

        ArrayList binArray = new ArrayList();

        try {
            UserAgent userAgent = new UserAgent();
            userAgent.visit(myurl);

            userAgent.doc.apply(fullAddress);
            userAgent.doc.submit("Search");


            Elements trs = userAgent.doc.findFirst("<table class = border>").findEvery("<tr>");

            int row = 0;

            for (Element tr : trs) {

                    Elements tds = tr.findEach("<td class = border>");

                    int col = 0;
                    bins[1] = "";

                    for (Element td : tds) {

                        String binText = td.findFirst("<center>").innerText(" ", false, false);


                        if (row == 0) {
                            continue;
                        } else {

                            switch (col)
                            {
                                case 0:

                                    binText = convertDate(binText);

                                    bins[0] = binText;

                                    if (row ==1) {
                                      firstDate = binText;
                                    }

                                    break;

                                case 1:
                                case 2:
                                    bins[1] += binText + "\n";
                                    break;

                                case 3:
                                    bins[1] += binText;
                                    break;
                            }

                            col += 1;
                        }
                    }
                row += 1;
                binArray.add(bins[0]);
                binArray.add(bins[1]);
            }

            binArray.remove(0);
            binArray.remove(0);

            return binArray;

        } catch (JauntException e) {
            System.err.println(e);
            return binArray;
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            launchSettings();
            return true;
        }

        if (id == R.id.action_input) {
            launchInput();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void launchSettings() {

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

    }

    public void launchInput() {

        Intent intent = new Intent(this, AddressActivity.class);
        startActivity(intent);
    }

}
