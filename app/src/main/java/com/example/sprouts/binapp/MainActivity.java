package com.example.sprouts.binapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.JauntException;
import com.jaunt.UserAgent;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/* TO DO: I need to update the binList each time the address is refreshed (in AddressActivity) *and* in the MainActivity when the app first opens, and at regular intervals using an alarm

I also need to convert the dates into proper dates so that I can make the alarm work... Don't use alarm use jobScheduler instead

 */

public class MainActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "Bins";
    private TextView postText;
    private TextView timeText;
    private String fullAddress;
    private String firstDate;
    private boolean reminder;
    private int min;

    private AlarmManager alarmMgr;

    private PendingIntent pendingIntent;

    ArrayAdapter<String> arrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        postText = (TextView) findViewById(R.id.textView);
        timeText = (TextView) findViewById(R.id.time);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    }


    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String address = prefs.getString("address", "DEFAULT");

        firstDate = prefs.getString("firstDate", "DEFAULT");

        reminder = prefs.getBoolean("pref_reminder", true);


        fullAddress = address;

        postText.setText(address);

        if (address.contains(",")) {

            String shortAddress = shortenAddress(address);

            postText.setText(shortAddress);

            check();

        } else {

            launchInput();

        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String binString = prefs.getString("bins", "DEFAULT");

        if (binString.contains(";")) {

            String[] bins = TextUtils.split(binString, ";");

            ArrayList<String> binList = new ArrayList(Arrays.asList(bins));

            ListView listView = (ListView) findViewById(R.id.listView);
            arrayAdapter = new MyAdapter(MainActivity.this, binList);
            listView.setAdapter(arrayAdapter);

        } else {
            launchInput();

        }

        if (firstDate != "DEFAULT") {
            setAlarm(firstDate);
        }

    }

    private void setAlarm(String date) {

        Calendar calendar = Calendar.getInstance();

        Calendar current = calendar;

        SimpleDateFormat inputFormat = new SimpleDateFormat("EEEE d MMMM yyyy");


        Date alarmDate = null;

        try {
            alarmDate = inputFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        calendar.setTime(alarmDate);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        SimpleDateFormat df1 = new SimpleDateFormat("dd");

        String day = df1.format(calendar.getTime());


        boolean alarmUp = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(MainActivity.this, MyReceiver.class), PendingIntent.FLAG_NO_CREATE) != null;

        if (reminder == true) {

            if (alarmUp) {
                timeText.setText("Alarm already set for " + day);
            } else {

                timeText.setText("New alarm set for " + day);

                Intent myIntent = new Intent(MainActivity.this, MyReceiver.class);

                pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, 0);

                alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);

                alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

            }
        } else {
            if (alarmUp) {
                stopService(new Intent(MainActivity.this, MyReceiver.class));
                timeText.setText("Alarm stopped");
            } else {
                timeText.setText("Alarm not set");
            }
        }
    }

    private Date extractDate(String binDate) {

        SimpleDateFormat inputFormat = new SimpleDateFormat("EEEE d MMMM yyyy");

        Date date = new Date();
        try {
            date = inputFormat.parse(binDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
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

        View coordinatorView = findViewById(R.id.coordinatorView);

                String stringUrl = "https://wastemanagementcalendar.cardiff.gov.uk/English.aspx";
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    new DownloadWebpageTask().execute(stringUrl);
                    Snackbar.make(coordinatorView, "Updating", Snackbar.LENGTH_SHORT).show();
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
                                    };

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
