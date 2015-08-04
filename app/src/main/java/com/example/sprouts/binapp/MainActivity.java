package com.example.sprouts.binapp;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaunt.Document;
import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.JauntException;
import com.jaunt.UserAgent;

import org.w3c.dom.DOMConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "Bins";
    private EditText postText;
    private ListView listView;
    private Button button;
    private TextView textView;


    ArrayList binArray = new ArrayList();

    ArrayAdapter<String> arrayAdapter;

    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.notification_template_icon_bg).setContentTitle("HELLO").setContentText("HELLOOO");


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        postText = (EditText) findViewById(R.id.postText);


        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final SharedPreferences.Editor edit = prefs.edit();

/*
        String postCode = prefs.getString("postcode", "DEFAULT");
        postText.setText(postCode);

*/

        /* work out what is going on here


        Set<String> set= prefs.getStringSet("bins", null);

        ArrayList binArray = new ArrayList(set);

*/
        /* try putStringSet and getStringSet - see bookmarked page*/

        postText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    edit.putString("postcode", postText.getText().toString());
                    edit.commit();

                    check();
                }
                return false;
            }
        });

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit.clear();
                edit.commit();
            }
        });
    }


    public void check() {


                String stringUrl = "https://wastemanagementcalendar.cardiff.gov.uk/English.aspx";
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    new DownloadWebpageTask().execute(stringUrl);
                  Toast.makeText(getApplicationContext(), "Updating", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Connecting", Toast.LENGTH_SHORT).show();
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


            ListView listView = (ListView) findViewById(R.id.listView);
            arrayAdapter = new MyAdapter(MainActivity.this, arrayList);
            listView.setAdapter(arrayAdapter);

        }
    }


    public void notification() {
        int mNotificationId = 001;

        NotificationManager mNot = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNot.notify(mNotificationId, mBuilder.build());
    }

    private ArrayList jaunty(String myurl) throws IOException {


        String[] bins = new String[2];

        ArrayList binArray = new ArrayList();

        try {
            UserAgent userAgent = new UserAgent();
            userAgent.visit(myurl);

            userAgent.doc.apply("47 / CF244QR");
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
