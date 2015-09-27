package com.example.sprouts.binapp;

import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.support.v7.app.*;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.util.*;

/* TO DO: I need to update the binList each time the address is refreshed (in AddressActivity) *and* in the MainActivity when the app first opens, and at regular intervals using an alarm

I also need to convert the dates into proper dates so that I can make the alarm work... Don't use alarm use jobScheduler instead

 */

public class MainActivity extends AppCompatActivity {

    private TextView postText;


    ArrayAdapter<String> arrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        postText = (TextView) findViewById(R.id.textView);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    }


    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String fullAddress = prefs.getString("address", "DEFAULT");
        String binString = prefs.getString("bins", "DEFAULT");

        if (fullAddress.contains(",") && binString.contains(";")) {

            String shortAddress = shortenAddress(fullAddress);

            postText.setText(shortAddress);

            launchIntent();
            updateBinList(binString);

        } else {

            launchInput();

        }

    }

    private void updateBinList(String binString) {

        String[] bins = TextUtils.split(binString, ";");

        ArrayList<String> binList = new ArrayList(Arrays.asList(bins));

        ListView listView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new MyAdapter(MainActivity.this, binList);
        listView.setAdapter(arrayAdapter);
    }

    private void launchIntent() {
        Intent updateIntent = new Intent(this, BgService.class);

        Log.v("SettingsFragment", "runIntent");

        this.startService(updateIntent);

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

        if (id == R.id.action_info) {
            launchInfo();
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

    public void launchInfo() {
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
    }

}
