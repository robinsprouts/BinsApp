package com.example.sprouts.networkapp;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "HttpExample";
    private EditText postText;
    private ListView listView;

    String[][] bins = new String[2][2];

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
        String postCode = prefs.getString("postcode", "");
        postText.setText(postCode);
    }


    public void click(View view) {
                String stringUrl = "https://wasteonlinecalendar.cardiff.gov.uk/";
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    new DownloadWebpageTask().execute(stringUrl);
                  /*  Toast.makeText(getApplicationContext(), "Updating", Toast.LENGTH_SHORT).show();
*/
                } else {
                    Toast.makeText(getApplicationContext(), "Connecting", Toast.LENGTH_SHORT).show();
                }
            }



    private class DownloadWebpageTask extends AsyncTask<String, Void, String[][]> {

        @Override
        protected String[][] doInBackground(String... urls) {

            String[][] a = new String[2][2];
            try {
                return jaunty(urls[0]);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                return a;
            }
        }

        @Override
        protected void onPostExecute(String[][] result) {

            bins = result;

            ArrayList arrayList = new ArrayList<String>(Arrays.asList(bins[0]));
            ArrayList arrayList2 = new ArrayList<String>(Arrays.asList(bins[1]));

            arrayList.addAll(arrayList2);

            ListView listView = (ListView) findViewById(R.id.listView);
            arrayAdapter = new MyAdapter(MainActivity.this, arrayList);

            listView.setAdapter(arrayAdapter);



             /* notification(); */


        }
    }

    public void notification() {
        int mNotificationId = 001;

        NotificationManager mNot = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNot.notify(mNotificationId, mBuilder.build());
    }

    private String[][] jaunty(String myurl) throws IOException {

        String bin;
        String[][] bins = new String[2][2];

        String postcode = postText.getText().toString();

        try {

            UserAgent userAgent = new UserAgent();

            userAgent.visit(myurl);
            userAgent.doc.apply(postcode);
            userAgent.doc.submit("Go");


            for (int n = 0; n<=1; n++) {
                Element dayOne = userAgent.doc.findFirst("<span id=lblCol" + (n + 1) + ">");


                bins[n][0] = dayOne.findFirst("<strong>").getText();
                bins[n][1] = "";


                Elements lis = dayOne.findEach("<li>");
                for (Element li : lis) {
                    bin = li.getText();
                    bins[n][1] += bin +"\n";
                }
            }

        } catch (JauntException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            //* return webpage;
            return bins;
        }
    }


    public String readIt(InputStream stream, int len) throws IOException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
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

        return super.onOptionsItemSelected(item);
    }

    public void launchSettings() {

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

    }

}
