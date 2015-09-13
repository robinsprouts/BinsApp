package com.example.sprouts.binapp;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.JauntException;
import com.jaunt.UserAgent;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class BackgroundService extends Service {


    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

    String fullAddress;
    String stringUrl;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.v("BGSERVICE", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        /*
        stringUrl = "https://wastemanagementcalendar.cardiff.gov.uk/English.aspx";
        String address = prefs.getString("address", "DEFAULT");

        fullAddress = address;

        new DownloadWebpageTask().execute(stringUrl);

        Log.v("BGSERVICE", "onStart");

        */

        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class DownloadWebpageTask extends AsyncTask<String, Void, ArrayList> {

        @Override

        protected ArrayList doInBackground(String... urls) {


            ArrayList<String> a = new ArrayList();

            try {
                Toast.makeText(getApplicationContext(), "Updating Bins", Toast.LENGTH_SHORT).show();
                return jaunty(urls[0]);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                return a;
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
                                    bins[0] = binText;
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
        protected void onPostExecute(ArrayList arrayList) {


            String joined = TextUtils.join(";", arrayList);

            String last;


            SimpleDateFormat formatDate = new SimpleDateFormat("EEEE d MMMM yyyy HH:mm:SS");

            last = formatDate.format(new Date());

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BackgroundService.this);

            final SharedPreferences.Editor edit = prefs.edit();
            edit.putString("bins", joined);
            edit.putString("last_fired", last);
            edit.commit();

        }
    }





}
