package com.example.sprouts.binapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.JauntException;
import com.jaunt.UserAgent;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BgService extends Service {
    public BgService() {
    }

    private String fullAddress;
    private String firstDate;

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

        check();

        broadcast();

        return START_NOT_STICKY;
    }

    private void broadcast() {

        sendBroadcast(new Intent("FINISHED"));

        Log.v("BGSERVICE", "broadcastSent");
    }

    private void setFired() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BgService.this);

        SimpleDateFormat formatDate = new SimpleDateFormat("EEEE d MMMM yyyy HH:mm:SS");

        String last = formatDate.format(new Date());

        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString("last_fired", last);
        edit.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("BGSERVICE", "onDestroy");
    }

    public void showNotification(String bin, String bigBin) {

        PendingIntent launchIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        int mNotificationId = 001;

        Notification.BigTextStyle big = new Notification.BigTextStyle();

        big.setSummaryText("summary")
                .setBigContentTitle("Bins updated!")
                .bigText(bigBin);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setStyle(big)
                .setSmallIcon(R.drawable.ic_stat_bin_notification)
                .setColor(0xFF00AA00)
                .setContentTitle("Bins updated!")
                .setContentText(bin)
                .setShowWhen(true)
                .setContentIntent(launchIntent);

        NotificationManager mNot = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNot.notify(mNotificationId, builder.build());
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
                return a;
            }
        }


        @Override
        protected void onPostExecute(ArrayList arrayList) {

            setFired();


          String bin = TextUtils.join(";", arrayList);
            String bigBin = TextUtils.join(";\n", arrayList);
            showNotification(bin, bigBin);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BgService.this);

            final SharedPreferences.Editor edit = prefs.edit();
            edit.putString("bins", bin);
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