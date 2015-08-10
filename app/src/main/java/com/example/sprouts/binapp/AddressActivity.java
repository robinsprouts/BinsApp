package com.example.sprouts.binapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.JauntException;
import com.jaunt.UserAgent;

import java.io.IOException;
import java.util.ArrayList;

public class AddressActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText address;
    private Button ok;
    private Spinner spinner;

    private String selectText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        address = (EditText) findViewById(R.id.address);
        ok = (Button) findViewById(R.id.ok);

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                spin();
            }
        };

        ok.setOnClickListener(onClickListener);

    }


    public void spin() {

        String stringUrl = "https://wastemanagementcalendar.cardiff.gov.uk/English.aspx";
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetAddressTask().execute(stringUrl);
            Toast.makeText(getApplicationContext(), "Updating", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Connecting", Toast.LENGTH_SHORT).show();
        }

    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


        selectText = (String) parent.getItemAtPosition(position);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AddressActivity.this);

        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString("address", selectText);
        edit.commit();


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class GetAddressTask extends AsyncTask<String, Void, ArrayList> {

        @Override

        protected ArrayList doInBackground(String... urls) {

            ArrayList<String> a = new ArrayList();

            try {
                return jaunty2(urls[0]);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                return a;
            }

        }

        @Override
        protected void onPostExecute(ArrayList arrayList) {

            if (arrayList.size() == 2) {
                arrayList.remove(0);
            }

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter(AddressActivity.this, android.R.layout.simple_spinner_dropdown_item, arrayList);

            spinner.setAdapter(arrayAdapter);

        }
    }

    private ArrayList jaunty2(String myurl) throws IOException {

        ArrayList<String> addressList = new ArrayList();

        String addString = address.getText().toString();

        try {

            UserAgent userAgent = new UserAgent();

            userAgent.visit(myurl);
            userAgent.doc.apply(addString);
            userAgent.doc.submit("Search");

            Element element = userAgent.doc.findFirst("<select id = droAddress>");

            element.findFirst("<option>");

            Elements options = element.findEach("<option>");


            for (Element option : options) {

                String text = option.getText();
                addressList.add(text);
            }


        } catch (JauntException e) {
            System.err.println(e);
            return addressList;
        }


        return addressList;

    }





    /* menu */


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