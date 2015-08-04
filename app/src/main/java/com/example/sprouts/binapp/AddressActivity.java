package com.example.sprouts.binapp;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jaunt.JauntException;
import com.jaunt.UserAgent;

import java.io.IOException;
import java.util.ArrayList;

public class AddressActivity extends AppCompatActivity {

    private EditText houseNumber;
    private EditText postCode;
    private Button ok;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        houseNumber = (EditText) findViewById(R.id.houseNumber);
        postCode = (EditText) findViewById(R.id.postCode);
        ok = (Button) findViewById(R.id.button);

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
        protected void onPostExecute(ArrayList arrayList){

        }
    }

    private ArrayList jaunty2(String myurl) throws IOException {

        ArrayList<String> addressList = new ArrayList();

        try {

            UserAgent userAgent = new UserAgent();

            userAgent.visit(myurl);
            userAgent.doc.apply("47 / CF244QR");
            userAgent.doc.submit("Search");

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
