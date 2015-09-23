package com.example.sprouts.binapp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.HttpResponse;
import com.jaunt.JauntException;
import com.jaunt.ResponseException;
import com.jaunt.UserAgent;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class BgService extends Service {
    public BgService() {
    }

    private String fullAddress;
    private String firstDate;
    private String secondDate;
    private String errorText = "Updated";

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("BGSERVICE", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.v("BGSERVICE", "onStartCommand");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        fullAddress = prefs.getString("address", "DEFAULT");
        firstDate = prefs.getString("firstDate", "DEFAULT");

        Log.v("BGSERVICE", firstDate);

        /*
        if (checkDate(firstDate)) {
        check();}
        else {
            errorText = "date not reached";
            broadcast();
        }
        */

        check();

        return START_NOT_STICKY;
    }

    private boolean checkDate(String date) {

        Log.v("BGSERVICE", "Check Date");

        SimpleDateFormat inputFormat = new SimpleDateFormat("EEEE d MMMM yyyy");

        Date firstDate = null;
        Date todayDate;

        try {
            firstDate = inputFormat.parse(date);

        } catch (ParseException e) {
            e.printStackTrace();

        }

        Calendar cal = Calendar.getInstance();
        todayDate = cal.getTime();


        if (todayDate.before(firstDate)) {
            Log.v("BGSERVICE", "before");
            return false;
        } else {
            Log.v("BGSERVICE", "after");
            return true;
        }

    }

    private void broadcast() {

        sendBroadcast(new Intent("FINISHED").putExtra("error", errorText));

        Log.v("BGSERVICE", "broadcastSent");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("BGSERVICE", "onDestroy");
    }

    public void check() {

        String stringUrl = "https://wastemanagementcalendar.cardiff.gov.uk/English.aspx";
            new DownloadWebpageTask().execute(stringUrl);
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, ArrayList> {

        @Override
        protected ArrayList doInBackground(String... urls) {


            ArrayList<String> a = new ArrayList();
            a.add(0, "error");

            try {
                return jaunty(urls[0]);
            } catch (IOException e) {
                return a;
            }
        }


        @Override
        protected void onPostExecute(ArrayList arrayList) {

                putList(arrayList);
                broadcast();
        }

    }

    private void putList(ArrayList arrayList) {

        String bin = TextUtils.join(";", arrayList);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BgService.this);

        final SharedPreferences.Editor edit = prefs.edit();

        String logText = prefs.getString("logText", "Empty Log");

        Calendar cal = Calendar.getInstance();

        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm:SS");

        String timeStamp = outputFormat.format(cal.getTime());

        if (errorText.equals("Updated")) {
            logText = logText + "\n" + "Update successful " + timeStamp;
        }
        else {
            logText = logText + "\n" + errorText + " " + timeStamp;
        }

        edit.putString("logText", logText);

        if (arrayList.get(0).toString().contains("Error")) {
        } else {
            edit.putString("bins", bin);
            edit.putString("firstDate", firstDate);
            edit.putString("secondDate", secondDate);
        }

        edit.commit();
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

                        switch (col) {
                            case 0:

                                binText = convertDate(binText);

                                bins[0] = binText;

                                if (row == 1) {
                                    firstDate = binText;
                                }

                                if (row == 2) {
                                    secondDate = binText;
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

        } catch (ResponseException e) {
                System.err.println(e);

                HttpResponse response = e.getResponse();
                if (response != null) {
                    errorText = "HTTP Error";
                }
                else {
                    errorText = "Connection Error";
                }
            binArray.add(0, errorText);
                return binArray;

        } catch (JauntException e) {
            System.err.println(e);
            binArray.add(0, "Error");
            return binArray;
        }


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
}