package com.example.sprouts.binapp;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.JauntException;
import com.jaunt.UserAgent;

import java.io.IOException;
import java.util.ArrayList;


public class BackgroundService extends JobService {

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    String fullAddress;

    @Override
    public boolean onStartJob(JobParameters params) {

        String stringUrl = "https://wastemanagementcalendar.cardiff.gov.uk/English.aspx";
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        String address = prefs.getString("address", "DEFAULT");

        fullAddress = address;

        new DownloadWebpageTask().execute(stringUrl);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        return false;
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

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BackgroundService.this);

            final SharedPreferences.Editor edit = prefs.edit();
            edit.putString("bins", joined);
            edit.commit();

        }
    }





}
